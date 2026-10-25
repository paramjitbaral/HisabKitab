import { Tabs, Redirect } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useLedger } from "../../lib/store";
import { C } from "../../components/ui";
export default function TabsLayout() {
  const { session, ready } = useLedger();
  if (!ready) return null;
  if (!session) return <Redirect href="/login" />;
  const icons = {
    add: "add-outline",
    loans: "document-text-outline",
    home: "home-outline",
    history: "time-outline",
    profile: "person-outline",
  } as const;
  return (
    <Tabs
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarStyle: { backgroundColor: C.green, borderTopWidth: 0 },
        tabBarActiveTintColor: route.name === "home" ? C.gold : "white",
        tabBarInactiveTintColor: route.name === "home" ? C.gold : "white",
        tabBarActiveBackgroundColor: "#ffffff12",
        tabBarIcon: ({ color, size }) => (
          <Ionicons
            name={icons[route.name as keyof typeof icons]}
            color={route.name === "home" ? C.gold : "white"}
            size={size}
          />
        ),
      })}
    >
      <Tabs.Screen name="add" options={{ title: "Add loan" }} />
      <Tabs.Screen name="loans" options={{ title: "Loans" }} />
      <Tabs.Screen name="home" options={{ title: "Home" }} />
      <Tabs.Screen name="history" options={{ title: "History" }} />
      <Tabs.Screen name="profile" options={{ title: "Profile" }} />
    </Tabs>
  );
}
