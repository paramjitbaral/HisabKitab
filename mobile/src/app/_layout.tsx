import { Stack, usePathname } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { LedgerProvider } from "../lib/store";
export default function Layout() {
  const path = usePathname();
  const lightSurface = path.includes("settings") || path.includes("otp");
  return <LedgerProvider>
    <StatusBar style={lightSurface ? "dark" : "light"}/>
    <Stack screenOptions={{ headerShown: false }}/>
  </LedgerProvider>;
}
