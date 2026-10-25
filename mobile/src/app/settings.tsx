import { useEffect, useState } from "react";
import { View, Text, ScrollView, Pressable, Linking, Platform } from "react-native";
import { router, useLocalSearchParams, Redirect } from "expo-router";

import { useSafeAreaInsets } from "react-native-safe-area-context";
import { storage, useLedger } from "../lib/store";
import { exportLoans } from "../lib/export";
import { styles, C, Field, Button, ErrorText, BackBar } from "../components/ui";


function Info({title,body}:{title:string;body:string}) {
  return <View style={{gap:7,borderBottomWidth:1,borderBottomColor:"#E4E8E6",paddingBottom:18}}><Text style={{fontSize:15,fontWeight:"600",color:C.ink}}>{title}</Text><Text style={{fontSize:14,lineHeight:21,color:C.muted}}>{body}</Text></View>;
}
export default function SettingsPage() {
  const {section} = useLocalSearchParams<{section:string}>();
  const {session,name,saveName,logout,loans,ready:sessionReady} = useLedger();
  const inset=useSafeAreaInsets();
  const [value,setValue]=useState(name),[email,setEmail]=useState(""),[photo,setPhoto]=useState(""),[error,setError]=useState(""),[busy,setBusy]=useState(false),[ready,setReady]=useState(false),[saved,setSaved]=useState(false);
  const key="profile-"+session?.user.id;
  useEffect(()=>{let mounted=true; void storage.get(key).then(raw=>{if(mounted && raw){const p=JSON.parse(raw);setEmail(p.email||"");setPhoto(p.photo||"");}}).catch(()=>{if(mounted)setError("Unable to load profile.");}).finally(()=>{if(mounted)setReady(true);});return()=>{mounted=false;};},[key]);
  function back(){if(router.canGoBack())router.back();else router.replace("/(tabs)/profile");}
  async function deviceSettings(){try{if(Platform.OS==="web"){setError("Open your browser’s site settings to review permissions.");return;}await Linking.openSettings();}catch{setError("Unable to open device settings.");}}
  async function save(){setBusy(true);setError("");try{await saveName(value);await storage.set(key,JSON.stringify({email:email.trim(),photo}));setSaved(true);}catch{setError("Unable to save profile.");}finally{setBusy(false);}}
  if(!sessionReady) return null;
  if(!session) return <Redirect href="/login"/>;
  return <View style={styles.page}>

    <BackBar onBack={back} title={section || "Settings"}/>
    <ScrollView contentContainerStyle={{padding:22,paddingBottom:inset.bottom+28,gap:22}} keyboardShouldPersistTaps="handled">
      {section==="Personal Information" ? <>
        <Field label="Your name" value={value} onChangeText={v=>{setValue(v);setSaved(false);}} maxLength={50}/>
        <Field label="Email (optional)" value={email} onChangeText={v=>{setEmail(v);setSaved(false);}} keyboardType="email-address" autoCapitalize="none" maxLength={150}/>
        <Info title="Phone number" body={session.user.phoneNumber}/>
        <Text style={styles.muted}>Your name, email and photo are saved on this device. To sign in with another phone number, sign out first.</Text>
        {ready && <Button title={saved?"Saved":"Save profile"} busy={busy} onPress={()=>void save()}/>}
      </> : section==="Your Data" ? <>
        <Info title="Your ledger" body={`${loans.length} loans · ${loans.filter(l=>!l.isRepaid).length} outstanding · ${loans.filter(l=>l.isRepaid).length} repaid`}/>
        <Info title="Export your loans" body="Save a CSV with names, amounts, due dates, repayment status and notes. Open it in Excel or Google Sheets."/>
        <Button title="Export loans as CSV" busy={busy} onPress={()=>{setBusy(true);setError("");void exportLoans(loans).catch(e=>setError(e instanceof Error?e.message:"Unable to export loans.")).finally(()=>setBusy(false));}}/>
        <Info title="Where your records live" body="Your loans are saved in the backend database under your account. Exports include the records currently loaded in the app, without OTPs or login credentials. Refresh your ledger before exporting to include the latest changes."/>
      </> : section==="Privacy & Security" ? <>
        <Info title="Your account" body="Loan records are associated with your signed-in phone number. Other accounts cannot access them through the API."/>
        <Info title="Your profile photo" body="You choose an image through the system photo picker. Your profile photo stays on this device."/>
        <Info title="Signing out" body="Signing out removes the saved session on this device. It does not delete your loan records."/>
        <Button title="Review device permissions" onPress={()=>void deviceSettings()}/>
        <Pressable accessibilityRole="button" onPress={()=>void logout().then(()=>router.replace("/login"))} style={{paddingVertical:14}}><Text style={{color:C.red,fontSize:14}}>Sign out of this account</Text></Pressable>
      </> : section==="Help & Support" ? <>
        <Info title="How do I add a loan?" body="Tap Add loan, enter your friend’s name, amount and due date, then save. Add an optional note to remember what it was for."/>
        <Info title="How do I record a repayment?" body="Once you receive the full amount, open the loan and mark it as repaid. This updates your records; it does not transfer money."/>
        <Info title="What is the difference between Loans and History?" body="Loans groups outstanding balances by friend. History includes both open and repaid entries."/>
        <Info title="When does a loan become overdue?" body="After its due date, if it has not been marked repaid."/>
        <Info title="Why can’t I see an OTP message?" body="This version uses mock OTP delivery. Read the newest six-digit code in the backend server log; no SMS is sent."/>
      </> : <><Text style={{fontSize:28,fontWeight:"600",color:C.green}}>MiniLedger</Text><Text style={{fontSize:15,lineHeight:22,color:C.muted}}>A small ledger for money lent to friends.</Text><Info title="Version" body="1.0"/><Info title="Made for everyday lending" body="Keep loan details together, track due dates and record repayments."/></>}
      <ErrorText message={error}/>
    </ScrollView>
  </View>;
}
