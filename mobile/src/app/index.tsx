import { Redirect } from "expo-router";
import { useLedger } from "../lib/store";
import { ActivityIndicator } from "react-native";
export default function Index() {
  const { session, ready } = useLedger();
  if (!ready) return <ActivityIndicator />;
  return <Redirect href={session ? "/(tabs)/home" : "/login"} />;
}
