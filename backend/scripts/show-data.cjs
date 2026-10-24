// Read-only demo viewer. Credentials stay in backend/.env and are never printed.
const path = require('node:path');
require('dotenv').config({ path: path.resolve(__dirname, '../.env') });
const { Pool } = require('pg');
const phone = process.argv[2];
if (!/^\+[1-9]\d{7,14}$/.test(phone || '')) {
  console.error('Usage: node backend/scripts/show-data.cjs +91YOURNUMBER [--watch]');
  process.exit(1);
}
const pool = new Pool({ connectionString: process.env.DATABASE_URL, options: '-c default_transaction_read_only=on' });
const watch = process.argv.includes('--watch');
let stopping = false;
process.on('SIGINT', () => { stopping = true; });
async function show() {
  if(watch) console.clear();
  const server = await pool.query('SELECT current_database() AS database, inet_server_addr() AS host, inet_server_port() AS port, version() AS version');
  console.log('LIVE POSTGRESQL — read-only —', new Date().toLocaleString());
  console.table(server.rows);
  console.log('\nOTP requests (mock codes; newest first)');
  console.table((await pool.query('SELECT id, phone_number, otp_code, created_at, expires_at, is_used, attempts FROM otps WHERE phone_number=$1 ORDER BY id DESC LIMIT 5', [phone])).rows);
  console.log('\nAccount created after successful OTP verification');
  console.table((await pool.query('SELECT id, phone_number, created_at FROM users WHERE phone_number=$1', [phone])).rows);
  console.log('\nLoans persisted for this account');
  console.table((await pool.query('SELECT l.id, l.friend_name, l.amount, l.due_date::text, l.note, l.is_repaid, l.repaid_at, l.created_at FROM loans l JOIN users u ON u.id=l.user_id WHERE u.phone_number=$1 ORDER BY l.id DESC', [phone])).rows);
  console.log('\nSummary calculated from those same PostgreSQL rows');
  console.table((await pool.query(`SELECT COALESCE(SUM(l.amount),0) AS total_lent, COALESCE(SUM(l.amount) FILTER(WHERE l.is_repaid),0) AS total_repaid, COALESCE(SUM(l.amount) FILTER(WHERE NOT l.is_repaid AND l.due_date < CURRENT_DATE),0) AS overdue_amount FROM loans l JOIN users u ON u.id=l.user_id WHERE u.phone_number=$1`, [phone])).rows);
}
(async () => {
  try { do { await show(); if(watch && !stopping) await new Promise(r=>setTimeout(r,2000)); } while(watch && !stopping); }
  catch(e) { console.error('Database viewer failed:', e.message); process.exitCode=1; }
  finally { await pool.end(); }
})();
