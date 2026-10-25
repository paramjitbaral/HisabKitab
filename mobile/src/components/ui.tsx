import React, { PropsWithChildren } from "react";
import {
  View,
  Text,
  Pressable,
  StyleSheet,
  TextInput,
  TextInputProps,
  ImageBackground,
  ImageSourcePropType,
  ActivityIndicator,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { Loan, money, overdue } from "../lib/api";
export const C = {
  green: "#073D2E",
  paper: "#FFFFFF",
  ink: "#10272A",
  muted: "#727C7B",
  gold: "#E6C572",
  red: "#B33E32",
};
export const styles = StyleSheet.create({
  page: { flex: 1, backgroundColor: C.paper },
  pad: { padding: 22, gap: 16 },
  title: { fontWeight: "600", fontSize: 28, color: C.ink },
  label: { fontSize: 12, color: C.ink, fontWeight: "600", marginBottom: 7 },
  input: {
    backgroundColor: "#FFFFFF",
    borderWidth: 1,
    borderColor: "#DDE3DF",
    borderRadius: 12,
    paddingHorizontal: 14,
    minHeight: 46,
    color: C.ink,
    fontSize: 14,
  },
  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  card: { padding: 18, borderRadius: 20, backgroundColor: "#FFFFFF", borderWidth:1, borderColor:"#E4E8E6", gap: 8 },
  sheet: { marginTop: -24, borderTopLeftRadius: 26, borderTopRightRadius: 26, backgroundColor: C.paper, minHeight: 400 },
  muted: { color: C.muted, fontSize: 13, lineHeight: 20 },
});
export function Button({
  title,
  onPress,
  busy = false,
}: {
  title: string;
  onPress: () => void;
  busy?: boolean;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      disabled={busy}
      onPress={onPress}
      style={{
        backgroundColor: C.green,
        borderRadius: 18,
        padding: 16,
        alignItems: "center",
        opacity: busy ? 0.6 : 1,
      }}
    >
      {busy ? (
        <ActivityIndicator color="white" />
      ) : (
        <Text style={{ color: "white", fontSize: 15, fontWeight: "600" }}>
          {title}
        </Text>
      )}
    </Pressable>
  );
}
export function Field({ label, ...props }: TextInputProps & { label: string }) {
  return (
    <View>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        accessibilityLabel={label}
        placeholderTextColor="#919895"
        style={styles.input}
        {...props}
      />
    </View>
  );
}
export function Hero({
  title,
  subtitle,
  image,
  children,
  onBack,
}: PropsWithChildren<{
  title: string;
  subtitle: string;
  image: ImageSourcePropType;
  onBack?: () => void;
}>) {
  const inset = useSafeAreaInsets();
  return (
    <ImageBackground
      source={image}
      style={{ minHeight: 220, paddingTop: onBack ? 0 : inset.top + 22, paddingBottom: 24 }}
    >
      <View
        style={{
          position: "absolute",
          top: 0,
          bottom: 0,
          left: 0,
          right: 0,
          backgroundColor: "#00000045",
        }}
      />
      {onBack && <BackBar onBack={onBack}/>}
      <View style={{ paddingHorizontal: 20, paddingTop: onBack ? 12 : 22, paddingBottom:16, gap: 8 }}>
        <Text style={[styles.title, { color: "white" }]}>{title}</Text>
        <Text style={{ color: "white", fontSize: 13, lineHeight: 19 }}>
          {subtitle}
        </Text>
        {children}
      </View>
    </ImageBackground>
  );
}
export function BackBar({onBack,title,titleColor=C.ink}:{onBack:()=>void;title?:string;titleColor?:string}) {
  const inset=useSafeAreaInsets();
  return <View style={{paddingTop:inset.top+8,paddingBottom:8,paddingHorizontal:20,flexDirection:"row",alignItems:"center",gap:12}}>
    <Pressable accessibilityRole="button" accessibilityLabel={title?"Back to profile":"Back"} onPress={onBack} style={{width:44,height:44,alignItems:"center",justifyContent:"center"}}><View style={{width:34,height:34,borderRadius:17,backgroundColor:"white",alignItems:"center",justifyContent:"center"}}><Ionicons name="arrow-back" size={20} color={C.ink}/></View></Pressable>
    {!!title && <Text style={{fontSize:20,fontWeight:"600",color:titleColor,flex:1}}>{title}</Text>}
  </View>;
}
export function ErrorText({ message }: { message: string }) {
  return message ? (
    <Text
      accessibilityRole="alert"
      style={{ color: C.red, paddingVertical: 8 }}
    >
      {message}
    </Text>
  ) : null;
}
export function LoanRow({
  loan,
  onRepay,
  busy,
  compact = false,
}: {
  loan: Loan;
  onRepay?: () => void;
  busy?: boolean;
  compact?: boolean;
}) {
  const late = overdue(loan);
  return (
    <View
      style={[
        styles.card,
        {
          padding: compact ? 13 : 18,
          gap: compact ? 5 : 8,
          backgroundColor: late
            ? "#FBE7E1"
            : loan.isRepaid
              ? "#E9F2E9"
              : "#FFFFFF",
        },
      ]}
    >
      <View style={styles.row}>
        <Text style={{ fontSize: compact ? 14 : 16, fontWeight: "600", color: C.ink }}>
          {loan.friendName}
        </Text>
        <Text style={{ color: late ? C.red : C.green, fontSize: 12 }}>
          {loan.isRepaid ? "Repaid" : late ? "Overdue" : "Pending"}
        </Text>
      </View>
      <Text style={{ fontSize: compact ? 18 : 21, color: C.ink }}>{money(loan.amount)}</Text>
      <Text style={styles.muted}>Due {loan.dueDate}</Text>
      {!!loan.note && <Text style={styles.muted}>{loan.note}</Text>}
      {onRepay && !loan.isRepaid && (
        <Pressable
          accessibilityRole="button"
          disabled={busy}
          onPress={onRepay}
          style={{ paddingVertical: 10 }}
        >
          <Text style={{ color: C.green, fontWeight: "600" }}>
            {busy ? "Saving…" : "Mark as repaid"}
          </Text>
        </Pressable>
      )}
    </View>
  );
}
