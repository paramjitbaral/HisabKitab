import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import authRoutes from './routes/auth';
import loanRoutes from './routes/loans';
import summaryRoutes from './routes/summary';
import { initDb } from './db';

dotenv.config();

const app = express();
const PORT = Number(process.env.PORT || process.env.BACKEND_PORT || 5001);

// Middleware
app.use(cors());
app.use(express.json({limit:'16kb'}));

// Routes
app.use('/auth', authRoutes);
app.use('/loans', loanRoutes);
app.use('/summary', summaryRoutes);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// Initialize database and start server
async function startServer() {
  try {
    await initDb();
    app.listen(PORT, () => {
      console.log(`\n============================================================`);
      console.log(`[MiniLedger] Backend Server running on http://localhost:${PORT}`);
      console.log(`[MiniLedger] PostgreSQL ready for queries`);
      console.log(`============================================================\n`);
    });
  } catch (error) {
    console.error('Fatal: Failed to initialize PostgreSQL database:', error);
    process.exit(1);
  }
}

if (require.main === module) startServer();

export default app;
