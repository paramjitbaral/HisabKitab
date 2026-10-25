import { ScrollView, View, Text, RefreshControl } from "react-native";
import { router } from "expo-router";
import { useLedger } from "../../lib/store";
import { money } from "../../lib/api";
import {
  Hero,
  styles,
  C,
  Button,
  LoanRow,
  ErrorText,
} from "../../components/ui";
export default function Home() {
  const { name, loans, summary, refresh, refreshing, error } = useLedger();
  return (
    <ScrollView
      style={styles.page}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={refresh} />
      }
    >
      <Hero
        title={"Hello, " + (name || "friend")}
        subtitle="Keep track of what you lend, in a simple way."
        image={require("../../../assets/home.png")}
      />
      <View style={styles.pad}>
        <View style={[styles.card, { backgroundColor: C.green }]}>
          <Text style={{ color: "white" }}>Total lent</Text>
          <Text style={[styles.title, { color: "white" }]}>
            {money(summary.totalLent)}
          </Text>
          <View style={styles.row}>
            <Text style={{ color: "#A9DEBD" }}>
              Repaid {money(summary.totalRepaid)}
            </Text>
            <Text style={{ color: "#FFB5A7" }}>
              Overdue {money(summary.overdueAmount)}
            </Text>
          </View>
        </View>
        <ErrorText message={error} />
        <View style={styles.row}>
          <Text style={styles.title}>Upcoming</Text>
        </View>
        {loans
          .filter((l) => !l.isRepaid)
          .sort((a, b) => a.dueDate.localeCompare(b.dueDate))
          .slice(0, 3)
          .map((l) => (
            <LoanRow key={l.id} loan={l} />
          ))}
        {!loans.length && (
          <Text style={styles.muted}>
            Your ledger starts here. Add your first loan.
          </Text>
        )}
        <Button
          title="Add a loan →"
          onPress={() => router.push("/(tabs)/add")}
        />
      </View>
    </ScrollView>
  );
}
