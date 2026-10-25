import { useState } from "react";
import {
  ScrollView,
  View,
  Text,
  Pressable,
  KeyboardAvoidingView,
  Platform,
  Image,
  useWindowDimensions,
} from "react-native";
import DateTimePicker from "@react-native-community/datetimepicker";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useLedger } from "../../lib/store";
import { dateKey } from "../../lib/api";
import { styles, C, Field, Button, ErrorText } from "../../components/ui";
export default function Add() {
  const { add } = useLedger();
  const [friendName, setFriend] = useState(""),
    [amount, setAmount] = useState(""),
    [dueDate, setDate] = useState(dateKey()),
    [note, setNote] = useState(""),
    [picker, setPicker] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const { height } = useWindowDimensions(),
    inset = useSafeAreaInsets();
  const canvas = Math.max(760, height - 85);
  async function save() {
    setBusy(true);
    setError("");
    try {
      await add({ friendName, amount: Number(amount), dueDate, note });
      setFriend("");
      setAmount("");
      setNote("");
      router.navigate("/(tabs)/home");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to save loan");
    } finally {
      setBusy(false);
    }
  }
  return (
    <KeyboardAvoidingView
      style={styles.page}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <ScrollView keyboardShouldPersistTaps="handled">
        <View style={{ minHeight: canvas }}>
          <Image
            source={require("../../../assets/paper.png")}
            resizeMode="stretch"
            style={{ position: "absolute", width: "100%", height: canvas }}
          />
          <View style={{ height: canvas * 0.42 }}>
            <Pressable
              accessibilityRole="button"
              accessibilityLabel="Back"
              onPress={() => router.navigate("/(tabs)/home")}
              style={{
                position: "absolute",
                left: 20,
                top: inset.top + 12,
                width: 34,
                height: 34,
                borderRadius: 17,
                backgroundColor: C.paper,
                justifyContent: "center",
                alignItems: "center",
              }}
            >
              <Ionicons name="arrow-back" size={20} color={C.ink} />
            </Pressable>
            <View
              style={{ position: "absolute", left: 22, top: canvas * 0.19 }}
            >
              <Text style={[styles.title, { fontSize: 28, color: "white" }]}>
                Add New Loan
              </Text>
              <Text
                style={{
                  color: "white",
                  fontSize: 12,
                  lineHeight: 18,
                  marginTop: 6,
                }}
              >
                {"A small help today,\na stronger bond tomorrow."}
              </Text>
            </View>
          </View>
          <View style={{ paddingHorizontal: 22, gap: 15, paddingBottom: 25 }}>
            <Field
              label="Friend’s Name"
              value={friendName}
              onChangeText={setFriend}
              placeholder="Enter friend’s name"
              maxLength={100}
            />
            <Field
              label="Amount (₹)"
              value={amount}
              onChangeText={setAmount}
              keyboardType="decimal-pad"
              placeholder="Enter amount"
            />
            <View style={styles.row}>
              {[500, 1000, 2000, 5000].map((n) => (
                <Pressable
                  key={n}
                  onPress={() => setAmount(String(n))}
                  style={{
                    paddingVertical: 7,
                    paddingHorizontal: 14,
                    backgroundColor: "#E8E2D7",
                    borderRadius: 20,
                  }}
                >
                  <Text style={{ fontSize: 11 }}>₹{n}</Text>
                </Pressable>
              ))}
            </View>
            {Platform.OS === "web" ? (
              <Field
                label="Due Date (YYYY-MM-DD)"
                value={dueDate}
                onChangeText={setDate}
              />
            ) : (
              <View>
                <Text style={styles.label}>Due Date</Text>
                <Pressable
                  accessibilityLabel="Choose due date"
                  onPress={() => setPicker(true)}
                  style={[styles.input, styles.row]}
                >
                  <Text style={{ paddingVertical: 14, color: C.ink }}>
                    {dueDate}
                  </Text>
                  <Ionicons name="calendar-outline" size={19} color={C.ink} />
                </Pressable>
              </View>
            )}
            {picker && (
              <DateTimePicker
                value={new Date(dueDate + "T12:00:00")}
                mode="date"
                onChange={(_, date) => {
                  setPicker(false);
                  if (date) setDate(dateKey(date));
                }}
              />
            )}
            <Field
              label="Note (Optional)"
              value={note}
              onChangeText={setNote}
              placeholder="Add a short note…"
              maxLength={500}
            />
            <ErrorText message={error} />
            <Button title="Save Loan →" onPress={save} busy={busy} />
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
