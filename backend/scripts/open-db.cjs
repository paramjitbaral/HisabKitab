// Open the installed PostgreSQL terminal without putting its password in commands.
const path = require('node:path');
const { spawnSync } = require('node:child_process');
require('dotenv').config({ path: path.resolve(__dirname, '../.env') });
if(!process.env.DATABASE_URL) throw Error('Configure backend/.env first');
const url = new URL(process.env.DATABASE_URL);
const psql = process.platform === 'win32' ? 'C:/Program Files/PostgreSQL/16/bin/psql.exe' : 'psql';
const child = spawnSync(psql, ['-h',url.hostname,'-p',url.port || '5432','-U',decodeURIComponent(url.username),'-d',decodeURIComponent(url.pathname.slice(1))], {
  stdio:'inherit', env:{...process.env, PGPASSWORD:decodeURIComponent(url.password), PGOPTIONS:'-c default_transaction_read_only=on'}
});
if(child.error) console.error(child.error.message);
process.exitCode = child.status ?? 1;
