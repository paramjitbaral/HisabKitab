import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';

import { jwtSecret } from '../config';

export interface AuthenticatedRequest extends Request {
  userId?: number;
  phoneNumber?: string;
}

export const authenticateUser = (
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Unauthorized: Missing or invalid token' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, jwtSecret!) as { userId: number; phoneNumber: string };
    req.userId = decoded.userId;
    req.phoneNumber = decoded.phoneNumber;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Unauthorized: Invalid or expired token' });
  }
};
