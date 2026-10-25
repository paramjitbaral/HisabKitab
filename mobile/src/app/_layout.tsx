import { Stack } from "expo-router";
import { useFonts } from "expo-font";
import { LedgerProvider } from "../lib/store";
import { ActivityIndicator } from "react-native";
export default function Layout() {
  const [loaded, error] = useFonts({
    Serif: require("../../assets/serif.ttf"),
  });
  if (!loaded && !error) return <ActivityIndicator />;
  return (
    <LedgerProvider>
      <Stack screenOptions={{ headerShown: false }} />
    </LedgerProvider>
  );
}
