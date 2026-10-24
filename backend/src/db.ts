import { PGlite } from '@electric-sql/pglite';
import { Pool } from 'pg';
import dotenv from 'dotenv';
import path from 'path';
import fs from 'fs';

dotenv.config();

let pgliteInstance: PGlite | null = null;
let pgPoolInstance: Pool | null = null;

const dataDir = process.env.PGLITE_DATA_DIR || path.join(__dirname, '../data/miniledger_pg');
if (!fs.existsSync(dataDir)) {
  fs.mkdirSync(dataDir, { recursive: true });
}

export async function initDb(): Promise<void> {
  const databaseUrl = process.env.DATABASE_URL;

  if (databaseUrl) {
      const pool = new Pool({ connectionString: databaseUrl });
      await pool.query('SELECT 1');
      pgPoolInstance = pool;
      console.log('[Database] Connected to external PostgreSQL via pg.Pool');
  }

  if (!pgPoolInstance && process.env.DB_MODE === 'pglite') {
    pgliteInstance = new PGlite(dataDir);
    await pgliteInstance.waitReady;
    console.log(`[Database] Initialized persistent PostgreSQL engine at: ${dataDir}`);
  }
  if (!pgPoolInstance && !pgliteInstance) throw new Error('Set DATABASE_URL for PostgreSQL, or explicitly set DB_MODE=pglite for local testing.');

  // Find schema.sql whether running in ts-node (src/) or compiled (dist/)
  const candidatePaths = [
    path.join(__dirname, 'schema.sql'),
    path.join(__dirname, '../src/schema.sql'),
    path.join(process.cwd(), 'backend/src/schema.sql'),
    path.join(process.cwd(), 'src/schema.sql')
  ];

  const foundPath = candidatePaths.find(p => fs.existsSync(p));
  if (foundPath) {
    const schemaSql = fs.readFileSync(foundPath, 'utf8');
    if (pgPoolInstance) {
      await pgPoolInstance.query(schemaSql);
    } else if (pgliteInstance) {
      await pgliteInstance.exec(schemaSql);
    }
    console.log(`[Database] PostgreSQL schema initialized from: ${foundPath}`);
  } else {
    console.error('[Database] Could not locate schema.sql at candidate paths:', candidatePaths);
  }
}

export async function closeDb() {
  await pgPoolInstance?.end();
  await pgliteInstance?.close();
}

export async function query(text: string, params: any[] = []): Promise<{ rows: any[] }> {
  if (pgPoolInstance) {
    const res = await pgPoolInstance.query(text, params);
    return { rows: res.rows };
  } else if (pgliteInstance) {
    const res = await pgliteInstance.query(text, params);
    return { rows: res.rows };
  } else {
    throw new Error('Database not initialized. Call initDb() first.');
  }
}
