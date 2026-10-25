import { Platform } from "react-native";
export const API_URL =
  process.env.EXPO_PUBLIC_API_URL ||
  (Platform.OS === "android"
    ? "http://10.0.2.2:5001"
    : "http://localhost:5001");
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message);
  }
}
export async function api<T>(
  path: string,
  method = "GET",
  body?: unknown,
  token?: string,
): Promise<T> {
  const response = await fetch(API_URL + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(15000),
  });
  const data = await response.json();
  if (!response.ok)
    throw new ApiError(data.error || "Request failed", response.status);
  return data as T;
}
export type Loan = {
  id: number;
  friendName: string;
  amount: number;
  dueDate: string;
  isRepaid: boolean;
  repaidAt?: string;
  createdAt: string;
  note: string;
};
export type Summary = {
  totalLent: number;
  totalRepaid: number;
  overdueAmount: number;
};
export const dateKey = (date = new Date()) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
export const overdue = (loan: Loan) =>
  !loan.isRepaid && loan.dueDate < dateKey();
export const money = (n: number) =>
  "₹ " + n.toLocaleString("en-IN", { maximumFractionDigits: 2 });
