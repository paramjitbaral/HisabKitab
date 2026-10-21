package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/** Portable, versioned backups contain loans only; never profile IDs or photo URIs. */
object LoanBackup {
    const val MAX_BYTES=5*1024*1024
    fun encode(loans:List<Loan>):String = JSONObject().put("app","HisabKitab").put("version",1).put("loans",JSONArray().apply {
        loans.forEach { loan -> put(JSONObject().put("friendName",loan.friendName).put("amount",loan.amount)
            .put("dueDateMillis",loan.dueDateMillis).put("isRepaid",loan.isRepaid)
            .put("repaidDateMillis",loan.repaidDateMillis ?: JSONObject.NULL).put("createdAtMillis",loan.createdAtMillis).put("note",loan.note)) }
    }).toString(2)
    fun decode(text:String):List<Loan> {
        require(text.toByteArray(Charsets.UTF_8).size<=MAX_BYTES) { "Backup must be smaller than 5 MB." }
        val json=JSONObject(text)
        require(json.getString("app")=="HisabKitab" && json.getInt("version")==1) { "Choose a HisabKitab version 1 backup." }
        val rows=json.getJSONArray("loans")
        require(rows.length()<=10000) { "Backup contains too many records." }
        return (0 until rows.length()).map { i ->
            val row=rows.getJSONObject(i)
            val name=row.getString("friendName").trim();val amount=row.getDouble("amount");val note=row.getString("note")
            val due=row.getLong("dueDateMillis");val created=row.getLong("createdAtMillis");val paid=row.getBoolean("isRepaid")
            val repaid=if(row.isNull("repaidDateMillis")) null else row.getLong("repaidDateMillis")
            require(name.length in 1..100 && note.length<=500 && amount.isFinite() && amount>0 && amount<=999999999.99 && kotlin.math.abs(amount*100-kotlin.math.round(amount*100))<0.0001) { "Invalid name, amount or note in backup." }
            require(due in 1..253402300799999L && created in 1..253402300799999L && (!paid || repaid!=null) && (repaid==null || repaid in 1..253402300799999L)) { "Invalid date in backup." }
            Loan(userId="",friendName=name,amount=amount,dueDateMillis=due,isRepaid=paid,repaidDateMillis=repaid,createdAtMillis=created,note=note)
        }
    }
}
