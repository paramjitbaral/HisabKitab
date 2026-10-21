package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Escape spreadsheet cells, including formula-like user-entered names and notes. */
fun loanCsv(loans:List<Loan>):String {
    fun cell(value:String):String {
        val safe=if(value.trimStart().firstOrNull() in listOf('=','+','-','@')) "'$value" else value
        return "\"${safe.replace("\"","\"\"")}\""
    }
    val date=SimpleDateFormat("yyyy-MM-dd",Locale.US)
    return "Friend,Amount (INR),Due date,Status,Repaid date,Note\r\n" + loans.joinToString("\r\n") { loan ->
        listOf(loan.friendName,String.format(Locale.US,"%.2f",loan.amount),date.format(Date(loan.dueDateMillis)),if(loan.isRepaid) "Repaid" else if(loan.isOverdue()) "Overdue" else "Pending",loan.repaidDateMillis?.let { date.format(Date(it)) }.orEmpty(),loan.note).joinToString(",",transform=::cell)
    }
}
