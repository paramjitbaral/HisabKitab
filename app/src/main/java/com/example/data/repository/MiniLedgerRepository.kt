package com.example.data.repository

import android.content.Context
import com.example.data.local.LoanDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Local-only storage. No network client, login or analytics. */
class MiniLedgerRepository(private val context:Context,private val loanDao:LoanDao) {
    private val prefs=context.getSharedPreferences("miniledger_prefs",Context.MODE_PRIVATE)
    private val photos=context.getSharedPreferences("miniledger_profiles",Context.MODE_PRIVATE)
    fun getStoredSession():UserSession? {
        val id=prefs.getString("user_phone",null) ?: return null
        return UserSession(id,prefs.getString("user_name","").orEmpty(),"")
    }
    fun ensureLocalProfile():UserSession {
        val previous=getStoredSession()
        val profile=previous?.copy(token="",name=previous.name.takeIf { it.any(Char::isLetter) && it!="User" }.orEmpty()) ?: UserSession("local-ledger","","")
        saveSession(profile)
        return profile
    }
    fun saveSession(session:UserSession) { prefs.edit().putString("user_phone",session.phoneNumber).putString("user_name",session.name).remove("auth_token").apply() }
    fun getProfilePhoto(id:String):String?=photos.getString(id,null)
    fun setProfilePhoto(id:String,uri:String?) { photos.edit().putString(id,uri).apply() }
    fun getLoansFlow(id:String)=loanDao.getLoansForUser(id)
    fun getSummaryFlow(id:String)=loanDao.getLoansForUser(id).map { loans -> LoanSummary(loans.sumOf { it.amount },loans.filter { it.isRepaid }.sumOf { it.amount },loans.filter { it.isOverdue() }.sumOf { it.amount }) }
    suspend fun addLoan(userId:String,friendName:String,amount:Double,dueDateMillis:Long,token:String?,note:String=""):Result<Loan> = withContext(Dispatchers.IO) { runCatching {
        require(friendName.trim().length in 1..100) { "Enter a name of 1–100 characters." }
        require(amount.isFinite() && amount>0 && amount<=999999999.99) { "Enter an amount between ₹0.01 and ₹999,999,999.99." }
        require(kotlin.math.abs(amount*100-kotlin.math.round(amount*100))<0.0001) { "Use at most two decimal places." }
        require(dueDateMillis>0 && note.length<=500) { "Check the date and note." }
        val loan=Loan(userId=userId,friendName=friendName.trim(),amount=amount,dueDateMillis=dueDateMillis,note=note.trim())
        loan.copy(id=loanDao.insertLoan(loan))
    } }
    suspend fun markLoanAsRepaid(id:Long,token:String?):Result<Boolean> = withContext(Dispatchers.IO) { runCatching { loanDao.markAsRepaid(id,System.currentTimeMillis());true } }
    suspend fun restore(id:String,loans:List<Loan>):Int=withContext(Dispatchers.IO) { loanDao.mergeBackup(id,loans) }
    suspend fun eraseAll()=withContext(Dispatchers.IO) {
        loanDao.deleteAll()
        prefs.edit().clear().commit();photos.edit().clear().commit()
        context.contentResolver.persistedUriPermissions.forEach { permission -> runCatching { context.contentResolver.releasePersistableUriPermission(permission.uri,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
    }
}
