import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useState } from "react";
import {
  ScrollView,
  View,
  Text,
  RefreshControl,
  Pressable,
} from "react-native";
import { useLedger } from "../../lib/store";
import { money } from "../../lib/api";
import { Hero, styles, LoanRow, ErrorText } from "../../components/ui";
export default function Loans() {
  const { loans, repay, refresh, refreshing, error } = useLedger(),
    [busy, setBusy] = useState<number | null>(null),
    [failure, setFailure] = useState("");
  const open = loans.filter((l) => !l.isRepaid),
    names = [...new Set(open.map((l) => l.friendName))];
  async function pay(id: number) {
    setBusy(id);
    try {
      await repay(id);
      setFailure("");
    } catch (e) {
      setFailure(e instanceof Error ? e.message : "Unable to update");
    } finally {
      setBusy(null);
    }
  }
  return (
    <ScrollView
      style={styles.page}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={refresh} />
      }
    >
      <Hero
        title="Outstanding"
        subtitle="Balances to collect, organised by person."
        image={require("../../../assets/wall.png")}
      >
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Back"
          onPress={() => router.navigate("/(tabs)/home")}
        >
          <Ionicons name="arrow-back-circle" size={34} color="white" />
        </Pressable>
      </Hero>
      <View style={[styles.sheet, {padding: 18, gap: 12}]}>
        <Text style={styles.muted}>Still to receive</Text>
        <Text style={[styles.title, {fontSize: 26}]}>
          {money(open.reduce((sum, l) => sum + l.amount, 0))}
        </Text>
        <ErrorText message={failure || error} />
        {!open.length && (
          <Text style={styles.muted}>
            All settled up. No outstanding loans.
          </Text>
        )}
        {names.map((name) => (
          <View key={name} style={{ gap: 10 }}>
            <Text style={{ fontSize: 15, fontWeight: "600" }}>{name}</Text>
            {open
              .filter((l) => l.friendName === name)
              .map((l) => (
                <LoanRow
                  compact
                  key={l.id}
                  loan={l}
                  busy={busy === l.id}
                  onRepay={() => pay(l.id)}
                />
              ))}
          </View>
        ))}
      </View>
    </ScrollView>
  );
}
