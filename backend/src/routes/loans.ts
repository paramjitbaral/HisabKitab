import { Router, Response } from 'express';
import { query } from '../db';
import { AuthenticatedRequest, authenticateUser } from '../middleware/auth';

const router = Router();

// Protect all loan routes
router.use(authenticateUser);

/**
 * GET /loans
 * Fetch the authenticated user's loan entries.
 */
router.get('/', async (req: AuthenticatedRequest, res: Response) => {
  try {
    const result = await query(
      `SELECT id, friend_name AS "friendName", amount::float, 
              TO_CHAR(due_date, 'YYYY-MM-DD') AS "dueDate", 
              is_repaid AS "isRepaid", 
              repaid_at AS "repaidAt", 
              created_at AS "createdAt", note
       FROM loans 
       WHERE user_id = $1 
       ORDER BY created_at DESC`,
      [req.userId]
    );

    return res.status(200).json(result.rows);
  } catch (error) {
    console.error('Error fetching loans:', error);
    return res.status(500).json({ error: 'Failed to fetch loan entries' });
  }
});

/**
 * POST /loans
 * Add a loan entry with friend's name, amount, and due date.
 */
router.post('/', async (req: AuthenticatedRequest, res: Response) => {
  const { friendName, amount, dueDate, note = '' } = req.body;

  // Validation
  if (!friendName || typeof friendName !== 'string' || friendName.trim() === '' || friendName.trim().length>100) {
    return res.status(400).json({ error: "Friend's name cannot be empty" });
  }

  const numericAmount = typeof amount==='number' || typeof amount==='string' ? Number(amount) : NaN;
  if (!Number.isFinite(numericAmount) || numericAmount <= 0 || numericAmount>=10000000000 || Math.abs(numericAmount*100-Math.round(numericAmount*100))>0.0001) {
    return res.status(400).json({ error: 'Amount must be a number greater than 0' });
  }

  if (typeof dueDate!=='string' || !/^\d{4}-\d{2}-\d{2}$/.test(dueDate) || !Number.isFinite(Date.parse(dueDate)) || new Date(dueDate).toISOString().slice(0,10)!==dueDate) {
    return res.status(400).json({ error: 'A valid due date is required (YYYY-MM-DD)' });
  }
  if(typeof note!=='string'||note.length>500) return res.status(400).json({error:'Note must be at most 500 characters.'});

  try {
    const result = await query(
      `INSERT INTO loans (user_id, friend_name, amount, due_date, note)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING id, friend_name AS "friendName", amount::float, 
                 TO_CHAR(due_date, 'YYYY-MM-DD') AS "dueDate", 
                 is_repaid AS "isRepaid", 
                 created_at AS "createdAt", note`,
      [req.userId, friendName.trim(), numericAmount, dueDate, note.trim()]
    );

    return res.status(201).json(result.rows[0]);
  } catch (error) {
    console.error('Error creating loan:', error);
    return res.status(500).json({ error: 'Failed to create loan entry' });
  }
});

/**
 * PATCH /loans/:id/repay
 * Mark a loan as fully repaid. Protected so only owner can modify.
 */
router.patch('/:id/repay', async (req: AuthenticatedRequest, res: Response) => {
  const loanId = Number(req.params.id);
  if (!Number.isSafeInteger(loanId) || loanId<=0) {
    return res.status(400).json({ error: 'Invalid loan ID' });
  }

  try {
    const result = await query(
      `UPDATE loans 
       SET is_repaid = TRUE, repaid_at = COALESCE(repaid_at,NOW())
       WHERE id = $1 AND user_id = $2
       RETURNING id, friend_name AS "friendName", amount::float, 
                 TO_CHAR(due_date, 'YYYY-MM-DD') AS "dueDate", 
                 is_repaid AS "isRepaid", 
                 repaid_at AS "repaidAt"`,
      [loanId, req.userId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Loan not found or unauthorized' });
    }

    return res.status(200).json(result.rows[0]);
  } catch (error) {
    console.error('Error repaying loan:', error);
    return res.status(500).json({ error: 'Failed to update loan status' });
  }
});

export default router;
