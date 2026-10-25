import React, { useState } from "react";
import {
  View,
  Text,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  Pressable,
} from "react-native";
import { Redirect } from "expo-router";
import { api } from "../lib/api";
import { useLedger } from "../lib/store";
import { Hero, Field, Button, ErrorText, C, styles } from "../components/ui";
export default function Login() {
  const { session, login } = useLedger();
  const [phone, setPhone] = useState("+91"),
    [code, setCode] = useState(""),
    [sent, setSent] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  if (session) return <Redirect href="/(tabs)/home" />;
  async function send() {
    setBusy(true);
    setError("");
    try {
      await api("/auth/send-otp", "POST", { phoneNumber: phone });
      setSent(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to send code");
    } finally {
      setBusy(false);
    }
  }
  async function verify() {
    setBusy(true);
    setError("");
    try {
      await login(phone, code);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to sign in");
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
        <Hero
          title="MiniLedger"
          subtitle={
            "Keep track of money you lend.\nSimple. Personal. Always with you."
          }
          image={require("../../assets/ledger.png")}
        />
        <View style={styles.pad}>
          <Text style={styles.title}>
            {sent ? "Verify your number" : "Welcome"}
          </Text>
          <Field
            label="Phone number"
            value={phone}
            onChangeText={setPhone}
            keyboardType="phone-pad"
            editable={!sent}
            placeholder="+919876543210"
          />
          {sent && (
            <>
              <Field
                label="6-digit OTP"
                value={code}
                onChangeText={setCode}
                keyboardType="number-pad"
                maxLength={6}
              />
              <Text style={styles.muted}>
                Mock OTP: read the code in the backend server log. It expires in
                5 minutes.
              </Text>
            </>
          )}
          <ErrorText message={error} />
          <Button
            title={sent ? "Verify OTP" : "Send OTP"}
            busy={busy}
            onPress={sent ? verify : send}
          />
          {sent && (
            <View style={styles.row}>
              <Pressable
                onPress={() => {
                  setSent(false);
                  setCode("");
                }}
              >
                <Text style={{ color: C.green, padding: 12 }}>
                  Change number
                </Text>
              </Pressable>
              <Pressable disabled={busy} onPress={send}>
                <Text style={{ color: C.green, padding: 12 }}>Resend code</Text>
              </Pressable>
            </View>
          )}
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
