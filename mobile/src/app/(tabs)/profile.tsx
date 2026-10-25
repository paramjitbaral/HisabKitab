import { useCallback, useState } from "react";
import { ScrollView, View, Text, Pressable, Image, Platform } from "react-native";
import { router, useFocusEffect } from "expo-router";
import { Ionicons } from "@expo/vector-icons";

import * as ImagePicker from "expo-image-picker";
import { File, Paths } from "expo-file-system";
import { storage, useLedger } from "../../lib/store";
import { styles, C, ErrorText, BackBar } from "../../components/ui";
const sections = [
  ["person-outline", "Personal Information"], ["download-outline", "Your Data"],
  ["shield-checkmark-outline", "Privacy & Security"],
  ["help-circle-outline", "Help & Support"], ["information-circle-outline", "About HisabKitab"],
] as const;
export default function Profile() {
  const { session, name, logout } = useLedger();

  const [email, setEmail] = useState(""), [photo, setPhoto] = useState(""), [error, setError] = useState("");
  const key = "profile-" + session?.user.id;
  useFocusEffect(useCallback(() => {
    let mounted = true;
    void storage.get(key).then(raw => { if(raw && mounted) { const p = JSON.parse(raw); setEmail(p.email || ""); setPhoto(p.photo || ""); } }).catch(() => { if(mounted) setError("Unable to load profile details."); });
    return () => { mounted = false; };
  }, [key]));
  async function choosePhoto() {
    try {
      const result = await ImagePicker.launchImageLibraryAsync({mediaTypes: ["images"], allowsEditing: true, aspect: [1,1], quality: .7, base64: Platform.OS === "web"});
      if(result.canceled) return;
      const asset = result.assets[0];
      let uri = asset.uri;
      if(Platform.OS === "web") uri = `data:${asset.mimeType || "image/jpeg"};base64,${asset.base64}`;
      else { const target = new File(Paths.document, `profile-${session?.user.id}-${Date.now()}.jpg`); await new File(uri).copy(target); uri = target.uri; }
      await storage.set(key, JSON.stringify({ email, photo: uri }));
      setPhoto(uri); setError("");
    } catch { setError("Unable to save that photo. Please try another."); }
  }
  return <ScrollView style={styles.page}>
    <View style={{height:240}}>
      <Image source={require("../../../assets/ledger.png")} style={{position:"absolute", width:"100%", height:"100%"}} resizeMode="cover" />
      <View style={{position:"absolute", top:0, bottom:0, left:0, right:0, backgroundColor:"#00000025"}} />
      <BackBar onBack={()=>router.navigate("/(tabs)/home")} title="Profile" titleColor="white"/>
    </View>
    <View style={{marginTop:-28, borderTopLeftRadius:28, borderTopRightRadius:28, backgroundColor:C.paper, paddingHorizontal:22, paddingBottom:24}}>
      <Pressable accessibilityRole="button" accessibilityLabel="Change profile photo" onPress={() => void choosePhoto()} style={{width:96,height:96,marginTop:-46,marginBottom:14}}>
        <View style={{width:96,height:96,borderRadius:48,backgroundColor:"#DFE4D7",borderWidth:2,borderColor:C.paper,overflow:"hidden",alignItems:"center",justifyContent:"center"}}>
          {photo ? <Image source={{uri:photo}} style={{width:"100%",height:"100%"}}/> : <Ionicons name="person-outline" size={44} color={C.green}/>}
        </View>
        <View style={{position:"absolute",right:-1,bottom:0,width:30,height:30,borderRadius:15,backgroundColor:C.green,borderWidth:1,borderColor:"white",alignItems:"center",justifyContent:"center"}}><Ionicons name="camera-outline" color="white" size={16}/></View>
      </Pressable>
      <View style={[styles.row,{alignItems:"flex-start",gap:8,marginBottom:20}]}>
        <View style={{flex:1,gap:4}}><Text style={{fontSize:23,fontWeight:"600",color:C.ink}}>{name || "Your profile"}</Text>{!!email && <Text style={styles.muted}>{email}</Text>}<Text style={styles.muted}>{session?.user.phoneNumber}</Text></View>
        <Pressable accessibilityRole="button" onPress={() => router.push({pathname:"/settings",params:{section:"Personal Information"}})} style={{borderWidth:1,borderColor:"#D5DCCE",borderRadius:9,padding:10,flexDirection:"row",gap:5}}><Ionicons name="create-outline" size={16} color={C.green}/><Text style={{color:C.green,fontSize:12}}>Edit Profile</Text></Pressable>
      </View>
      <ErrorText message={error}/>
      {sections.map(([icon,label]) => <View key={label}>
        <Pressable accessibilityRole="button" onPress={() => router.push({pathname:"/settings",params:{section:label}})} style={{flexDirection:"row",alignItems:"center",gap:14,borderTopWidth:1,borderTopColor:"#E4E8E6",paddingVertical:14}}>
          <View style={{width:38,height:38,borderRadius:19,backgroundColor:"#DFE4D7",alignItems:"center",justifyContent:"center"}}><Ionicons name={icon} size={23} color={C.green}/></View>
          <Text style={{flex:1,fontSize:15,color:C.ink}}>{label}</Text><Ionicons name="chevron-forward" size={18} color={C.muted}/>
        </Pressable>
      </View>)}
      <Pressable accessibilityRole="button" onPress={() => void logout()} style={{marginTop:18,backgroundColor:"#F5E4DC",borderRadius:14,padding:17,flexDirection:"row",alignItems:"center",gap:14}}><Ionicons name="log-out-outline" size={23} color={C.red}/><Text style={{flex:1,color:C.red,fontSize:15}}>Logout</Text><Ionicons name="chevron-forward" size={18} color={C.red}/></Pressable>
    </View>
  </ScrollView>;
}
