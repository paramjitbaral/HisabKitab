import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  PropsWithChildren,
} from "react";
import { Platform } from "react-native";
import * as SecureStore from "expo-secure-store";
import { api, ApiError, Loan, Summary } from "./api";
type Session = { token: string; user: { id: number; phoneNumber: string } };
export const storage = {
  get: async (key: string) =>
    Platform.OS === "web"
      ? sessionStorage.getItem(key)
      : SecureStore.getItemAsync(key),
  set: async (key: string, value: string) => {
    if (Platform.OS === "web") sessionStorage.setItem(key, value);
    else await SecureStore.setItemAsync(key, value);
  },
  remove: async (key: string) => {
    if (Platform.OS === "web") sessionStorage.removeItem(key);
    else await SecureStore.deleteItemAsync(key);
  },
};
function useLedgerState() {
  const [session, setSession] = useState<Session | null>(null),
    [ready, setReady] = useState(false),
    [loans, setLoans] = useState<Loan[]>([]),
    [summary, setSummary] = useState<Summary>({
      totalLent: 0,
      totalRepaid: 0,
      overdueAmount: 0,
    }),
    [error, setError] = useState(""),
    [refreshing, setRefreshing] = useState(false),
    [name, setName] = useState("");
  useEffect(() => {
    storage
      .get("session")
      .then((value) => {
        if (value) setSession(JSON.parse(value));
      })
      .catch(() => setError("Please sign in again."))
      .finally(() => setReady(true));
  }, []);
  const logout = useCallback(async () => {
    await storage.remove("session");
    setSession(null);
    setLoans([]);
    setSummary({ totalLent: 0, totalRepaid: 0, overdueAmount: 0 });
    setName("");
    setError("");
  }, []);
  const refresh = useCallback(async () => {
    if (!session) return;
    setRefreshing(true);
    try {
      const [items, totals] = await Promise.all([
        api<Loan[]>("/loans", "GET", undefined, session.token),
        api<Summary>("/summary", "GET", undefined, session.token),
      ]);
      setLoans(items);
      setSummary(totals);
      setError("");
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) await logout();
      setError(e instanceof Error ? e.message : "Unable to load loans");
    } finally {
      setRefreshing(false);
    }
  }, [session, logout]);
  // Fetch the authenticated server state whenever the restored session changes.
  useEffect(() => {
    // Refresh starts network synchronization and exposes its loading state.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void refresh();
    if (session)
      void storage.get("name-" + session.user.id).then((v) => setName(v || ""));
  }, [refresh, session]);
  async function login(phoneNumber: string, otp: string) {
    const next = await api<Session>("/auth/verify-otp", "POST", {
      phoneNumber,
      otp,
    });
    await storage.set("session", JSON.stringify(next));
    setSession(next);
  }
  async function add(input: {
    friendName: string;
    amount: number;
    dueDate: string;
    note: string;
  }) {
    if (!session) throw Error("Please sign in");
    await api("/loans", "POST", input, session.token);
    await refresh();
  }
  async function repay(id: number) {
    if (!session) throw Error("Please sign in");
    await api(`/loans/${id}/repay`, "PATCH", {}, session.token);
    await refresh();
  }
  async function saveName(value: string) {
    if (session) {
      await storage.set("name-" + session.user.id, value.trim());
      setName(value.trim());
    }
  }
  return {
    session,
    ready,
    loans,
    summary,
    error,
    refreshing,
    name,
    login,
    logout,
    refresh,
    add,
    repay,
    saveName,
  };
}
const Context = createContext<ReturnType<typeof useLedgerState> | null>(null);
export function LedgerProvider({ children }: PropsWithChildren) {
  const state = useLedgerState();
  return <Context.Provider value={state}>{children}</Context.Provider>;
}
export const useLedger = () => {
  const value = useContext(Context);
  if (!value) throw Error("Missing provider");
  return value;
};
