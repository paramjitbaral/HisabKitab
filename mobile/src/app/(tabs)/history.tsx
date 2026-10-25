import { useState } from "react";
import { router } from "expo-router";
import {
  ScrollView,
  View,
  Text,
  Pressable,
  RefreshControl,
} from "react-native";
import { useLedger } from "../../lib/store";
import { overdue } from "../../lib/api";
import { Hero, styles, C, LoanRow, ErrorText } from "../../components/ui";
export default function History() {
  const { loans, refresh, refreshing, error } = useLedger(),
    [filter, setFilter] = useState("All");
  const visible = loans.filter(
    (l) =>
      filter === "All" ||
      (filter === "Repaid"
        ? l.isRepaid
        : filter === "Overdue"
          ? overdue(l)
          : !l.isRepaid && !overdue(l)),
  );
  return (
    <ScrollView
      style={styles.page}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={refresh} />
      }
    >
      <Hero
        onBack={()=>router.navigate("/(tabs)/home")}
        title="Loan History"
        subtitle="All your lending activity in one place."
        image={require("../../../assets/ledger.png")}
      />
      <View style={[styles.pad, styles.sheet]}>
        <View style={styles.row}>
          {["All", "Pending", "Repaid", "Overdue"].map((f) => (
            <Pressable
              onPress={() => setFilter(f)}
              key={f}
              style={{
                backgroundColor: filter === f ? C.green : "#F2F4F3",
                borderRadius: 18,
                padding: 10,
              }}
            >
              <Text
                style={{ fontSize: 12, color: filter === f ? "white" : C.ink }}
              >
                {f}
              </Text>
            </Pressable>
          ))}
        </View>
        <ErrorText message={error} />
        {visible.map((l) => (
          <View key={l.id} style={{ gap: 6 }}>
            <Text style={styles.muted}>
              {new Date(l.repaidAt || l.createdAt).toLocaleDateString()}
            </Text>
            <LoanRow loan={l} />
          </View>
        ))}
        {!visible.length && (
          <Text style={styles.muted}>No entries in this view.</Text>
        )}
      </View>
    </ScrollView>
  );
}
