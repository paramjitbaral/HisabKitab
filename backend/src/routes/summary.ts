import { Router, Response } from 'express';
import { query } from '../db';
import { AuthenticatedRequest, authenticateUser } from '../middleware/auth';

const router = Router();

// Protect summary endpoint
router.use(authenticateUser);

/**
 * GET /summary
 * Return total lent, total repaid, and overdue amount.
 * 
 * - Total lent: Sum of every loan amount created.
 * - Total repaid: Sum of loan amounts for entries marked as repaid.
 * - Overdue amount: Sum of unpaid loan amounts whose due dates are before current date.
 */
router.get('/', async (req: AuthenticatedRequest, res: Response) => {
  try {
    const result = await query(
      `SELECT 
         COALESCE(SUM(amount), 0)::float AS "totalLent",
         COALESCE(SUM(CASE WHEN is_repaid = TRUE THEN amount ELSE 0 END), 0)::float AS "totalRepaid",
         COALESCE(SUM(CASE WHEN is_repaid = FALSE AND due_date < CURRENT_DATE THEN amount ELSE 0 END), 0)::float AS "overdueAmount"
       FROM loans
       WHERE user_id = $1`,
      [req.userId]
    );

    const summary = result.rows[0] || {
      totalLent: 0,
      totalRepaid: 0,
      overdueAmount: 0
    };

    return res.status(200).json(summary);
  } catch (error) {
    console.error('Error calculating summary:', error);
    return res.status(500).json({ error: 'Failed to calculate summary' });
  }
});

export default router;
