import { Platform } from "react-native";
import { File, Paths } from "expo-file-system";
import * as Sharing from "expo-sharing";
import { Loan, overdue } from "./api";

export function loanCsv(loans: Loan[]): string {
  const cell = (value: string) => {
    const safe = /^[\s]*[=+@-]/.test(value) ? "'" + value : value;
    return '"' + safe.replace(/"/g, '""') + '"';
  };
  return "Friend,Amount (INR),Due date,Status,Repaid date,Note\r\n" + loans.map(l =>
    [l.friendName, l.amount.toFixed(2), l.dueDate, l.isRepaid ? "Repaid" : overdue(l) ? "Overdue" : "Pending", l.repaidAt?.slice(0,10) || "", l.note].map(cell).join(",")
  ).join("\r\n");
}

export async function exportLoans(loans: Loan[]) {
  const csv = loanCsv(loans);
  if (Platform.OS === "web") {
    const url = URL.createObjectURL(new Blob(["\uFEFF", csv], {type:"text/csv;charset=utf-8"}));
    const link = document.createElement("a");
    link.href=url; link.download="HisabKitab-loans.csv";
    document.body.appendChild(link); link.click(); link.remove();
    setTimeout(()=>URL.revokeObjectURL(url),1000);
    return;
  }
  if (!await Sharing.isAvailableAsync()) throw new Error("File sharing is unavailable on this device.");
  const file = new File(Paths.cache, "HisabKitab-loans.csv");
  await file.write("\uFEFF" + csv);
  await Sharing.shareAsync(file.uri, {mimeType:"text/csv", UTI:"public.comma-separated-values-text", dialogTitle:"Export loans"});
}
