"use client";

import { useEffect, useMemo, useState } from "react";
import {
    DeliveryResponse,
    Status,
    TranslationRecord,
    createRecord,
    deliverRecord,
    listRecords,
    translateRecord,
} from "@/lib/api";

const LANGS = ["en", "es", "fr", "pt"] as const;

function formatFlow(r: TranslationRecord) {
    return `${r.sourceLang} → ${r.targetLang}`;
}

function prettyTime(iso: string) {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return iso;
    return d.toLocaleString();
}

export default function Page() {
    const [sourceLang, setSourceLang] = useState<(typeof LANGS)[number]>("en");
    const [targetLang, setTargetLang] = useState<(typeof LANGS)[number]>("pt");
    const [text, setText] = useState("");

    const [result, setResult] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [history, setHistory] = useState<TranslationRecord[]>([]);
    const [selectedId, setSelectedId] = useState<number | null>(null);
    const [historyFilter, setHistoryFilter] = useState<"ALL" | Status>("ALL");

    const [loading, setLoading] = useState(false);
    const [historyLoading, setHistoryLoading] = useState(false);

    const [delivering, setDelivering] = useState(false);
    const [delivery, setDelivery] = useState<DeliveryResponse | null>(null);

    const selected = useMemo(
        () => history.find((h) => h.id === selectedId) || null,
        [history, selectedId]
    );

    async function refreshHistory(filter = historyFilter) {
        setHistoryLoading(true);
        setError(null);
        try {
            const items =
                filter === "ALL" ? await listRecords() : await listRecords(filter);
            items.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
            setHistory(items);
        } catch (e: any) {
            setError(e?.message || "Couldn’t load history.");
        } finally {
            setHistoryLoading(false);
        }
    }

    useEffect(() => {
        refreshHistory();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    async function onTranslate() {
        setError(null);
        setResult(null);
        setDelivery(null);

        if (!text.trim()) {
            setError("Type something first.");
            return;
        }

        setLoading(true);
        try {
            const created = await createRecord({
                originalText: text,
                sourceLang,
                targetLang,
            });

            const translated = await translateRecord(created.id);

            setResult(translated.translatedText);
            await refreshHistory();
            setSelectedId(translated.id);
        } catch (e: any) {
            setError(e?.message || "Something went wrong.");
        } finally {
            setLoading(false);
        }
    }

    async function onDeliver() {
        if (!selected) return;
        setError(null);
        setDelivering(true);
        setDelivery(null);

        try {
            const res = await deliverRecord(selected.id);
            setDelivery(res);
        } catch (e: any) {
            setError(e?.message || "Delivery failed.");
        } finally {
            setDelivering(false);
        }
    }

    function clearAll() {
        setText("");
        setResult(null);
        setError(null);
        setDelivery(null);
    }

    function onChangeFilter(v: "ALL" | Status) {
        setHistoryFilter(v);
        setSelectedId(null);
        setDelivery(null);
        refreshHistory(v);
    }

    return (
        <main className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-emerald-50 text-zinc-900">
            <div className="mx-auto max-w-5xl px-6 py-10">
                <header className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                        <h1 className="text-3xl font-semibold tracking-tight">
                            Translate text
                        </h1>
                        <p className="mt-1 text-sm text-zinc-600">
                            Translate, browse history, and deliver results to a partner.
                        </p>
                    </div>
                </header>

                <section className="grid gap-6 lg:grid-cols-2">
                    <div className="rounded-2xl bg-white/80 p-6 shadow-sm ring-1 ring-zinc-200 backdrop-blur">
                        <div className="flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Translate</h2>
                            <span className="text-xs text-zinc-500">Simple workflow</span>
                        </div>

                        <div className="mt-5 grid gap-4 sm:grid-cols-2">
                            <label className="flex flex-col gap-2">
                <span className="text-sm font-medium text-zinc-700">
                  Source language
                </span>
                                <select
                                    className="h-11 rounded-xl border border-zinc-200 bg-white px-3 text-zinc-900 shadow-sm outline-none focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
                                    value={sourceLang}
                                    onChange={(e) => setSourceLang(e.target.value as any)}
                                >
                                    {LANGS.map((l) => (
                                        <option key={l} value={l}>
                                            {l}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label className="flex flex-col gap-2">
                <span className="text-sm font-medium text-zinc-700">
                  Target language
                </span>
                                <select
                                    className="h-11 rounded-xl border border-zinc-200 bg-white px-3 text-zinc-900 shadow-sm outline-none focus:border-emerald-300 focus:ring-4 focus:ring-emerald-100"
                                    value={targetLang}
                                    onChange={(e) => setTargetLang(e.target.value as any)}
                                >
                                    {LANGS.map((l) => (
                                        <option key={l} value={l}>
                                            {l}
                                        </option>
                                    ))}
                                </select>
                            </label>
                        </div>

                        <label className="mt-4 flex flex-col gap-2">
                            <span className="text-sm font-medium text-zinc-700">Text</span>
                            <textarea
                                className="min-h-[150px] resize-y rounded-xl border border-zinc-200 bg-white px-3 py-3 text-zinc-900 shadow-sm outline-none placeholder:text-zinc-400 focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
                                placeholder="Type something…"
                                value={text}
                                onChange={(e) => setText(e.target.value)}
                            />
                        </label>

                        <div className="mt-4 flex flex-wrap items-center gap-3">
                            <button
                                onClick={onTranslate}
                                disabled={loading}
                                className="inline-flex h-11 items-center justify-center rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 px-5 text-sm font-semibold text-white shadow-sm transition hover:brightness-110 disabled:opacity-60"
                            >
                                {loading ? "Translating…" : "Translate"}
                            </button>

                            <button
                                onClick={clearAll}
                                disabled={loading}
                                className="inline-flex h-11 items-center justify-center rounded-xl border border-zinc-200 bg-white px-5 text-sm font-semibold text-zinc-800 shadow-sm transition hover:bg-zinc-50 disabled:opacity-60"
                            >
                                Clear
                            </button>

                            <button
                                onClick={() => refreshHistory()}
                                disabled={historyLoading}
                                className="inline-flex h-11 items-center justify-center rounded-xl border border-zinc-200 bg-white px-5 text-sm font-semibold text-zinc-800 shadow-sm transition hover:bg-zinc-50 disabled:opacity-60"
                            >
                                {historyLoading ? "Refreshing…" : "Refresh history"}
                            </button>
                        </div>

                        {error && (
                            <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                                {error}
                            </div>
                        )}
                    </div>

                    <div className="rounded-2xl bg-white/80 p-6 shadow-sm ring-1 ring-zinc-200 backdrop-blur">
                        <div className="flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Result</h2>
                            <span className="text-xs text-zinc-500">After translation</span>
                        </div>

                        <div className="mt-5 rounded-2xl bg-gradient-to-br from-zinc-50 to-white p-5 ring-1 ring-zinc-200">
                            {result === null ? (
                                <div className="text-sm text-zinc-600">
                                    Translate something to see the result here.
                                </div>
                            ) : (
                                <div className="whitespace-pre-wrap text-base leading-7 text-zinc-900">
                                    {result}
                                </div>
                            )}
                        </div>

                        <div className="mt-5 rounded-2xl bg-white p-5 ring-1 ring-zinc-200">
                            <div className="flex items-center justify-between">
                                <div className="font-semibold">Deliver to partner</div>
                                <span className="text-xs text-zinc-500">
                  Available when done
                </span>
                            </div>

                            <p className="mt-2 text-sm text-zinc-600">
                                This simulates sending the translation to another system.
                            </p>

                            <button
                                onClick={onDeliver}
                                disabled={!selected || selected.status !== "TRANSLATED" || delivering}
                                className="mt-4 inline-flex h-11 items-center justify-center rounded-xl bg-emerald-600 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:opacity-60"
                            >
                                {delivering ? "Delivering…" : "Deliver"}
                            </button>

                            {delivery && (
                                <div className="mt-4">
                                    <div className="text-xs font-semibold uppercase tracking-wide text-zinc-500">
                                        Delivered payload
                                    </div>
                                    <pre className="mt-2 max-h-[220px] overflow-auto rounded-xl bg-zinc-900 p-4 text-xs text-zinc-100">
{JSON.stringify(delivery, null, 2)}
                  </pre>
                                </div>
                            )}
                        </div>
                    </div>
                </section>

                <section className="mt-8 grid gap-6 lg:grid-cols-2">
                    <div className="rounded-2xl bg-white/80 p-6 shadow-sm ring-1 ring-zinc-200 backdrop-blur">
                        <div className="flex items-center justify-between gap-3">
                            <h2 className="text-lg font-semibold">Translation history</h2>

                            <div className="flex items-center gap-2">
                                <span className="text-xs text-zinc-600">Filter</span>
                                <select
                                    className="h-10 rounded-xl border border-zinc-200 bg-white px-3 text-sm text-zinc-900 shadow-sm outline-none focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
                                    value={historyFilter}
                                    onChange={(e) => onChangeFilter(e.target.value as any)}
                                >
                                    <option value="ALL">All</option>
                                    <option value="CREATED">Created</option>
                                    <option value="TRANSLATED">Translated</option>
                                </select>
                            </div>
                        </div>

                        {history.length === 0 && !historyLoading && (
                            <p className="mt-4 text-sm text-zinc-600">No history yet.</p>
                        )}

                        <ul className="mt-4 space-y-2">
                            {history.map((h) => {
                                const active = selectedId === h.id;
                                return (
                                    <li key={h.id}>
                                        <button
                                            onClick={() => {
                                                setSelectedId(h.id);
                                                setDelivery(null);
                                            }}
                                            className={[
                                                "w-full rounded-2xl border px-4 py-3 text-left shadow-sm transition",
                                                active
                                                    ? "border-indigo-300 bg-indigo-50"
                                                    : "border-zinc-200 bg-white hover:bg-zinc-50",
                                            ].join(" ")}
                                        >
                                            <div className="flex items-center justify-between gap-3">
                                                <div className="font-semibold">{formatFlow(h)}</div>
                                                <span
                                                    className={`rounded-full px-3 py-1 text-xs font-semibold ${
                                                        h.status === "TRANSLATED"
                                                            ? "bg-emerald-100 text-emerald-800"
                                                            : h.status === "FAILED"
                                                                ? "bg-red-100 text-red-800"
                                                                : "bg-indigo-100 text-indigo-800"
                                                    }`}
                                                >
                          {h.status}
                        </span>
                                            </div>
                                            <div className="mt-1 line-clamp-1 text-sm text-zinc-600">
                                                {h.originalText}
                                            </div>
                                            <div className="mt-2 text-xs text-zinc-500">
                                                {prettyTime(h.createdAt)}
                                            </div>
                                        </button>
                                    </li>
                                );
                            })}
                        </ul>
                    </div>

                    <div className="rounded-2xl bg-white/80 p-6 shadow-sm ring-1 ring-zinc-200 backdrop-blur">
                        <div className="flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Details</h2>
                            <span className="text-xs text-zinc-500">Click an item</span>
                        </div>

                        {!selected ? (
                            <div className="mt-5 rounded-2xl bg-zinc-50 p-5 text-sm text-zinc-600 ring-1 ring-zinc-200">
                                Pick something from history to see the full content.
                            </div>
                        ) : (
                            <div className="mt-5 space-y-4">
                                <div className="flex items-center justify-between">
                                    <div className="font-semibold">{formatFlow(selected)}</div>
                                    <span className="text-xs text-zinc-500">ref: {selected.id}</span>
                                </div>

                                <div className="rounded-2xl bg-white p-5 ring-1 ring-zinc-200">
                                    <div className="text-xs font-semibold uppercase tracking-wide text-zinc-500">
                                        Original
                                    </div>
                                    <div className="mt-2 whitespace-pre-wrap text-sm leading-6">
                                        {selected.originalText}
                                    </div>
                                </div>

                                <div className="rounded-2xl bg-white p-5 ring-1 ring-zinc-200">
                                    <div className="text-xs font-semibold uppercase tracking-wide text-zinc-500">
                                        Translated
                                    </div>
                                    <div className="mt-2 whitespace-pre-wrap text-sm leading-6">
                                        {selected.translatedText ?? "(not translated yet)"}
                                    </div>
                                    {selected.translatedAt && (
                                        <div className="mt-3 text-xs text-zinc-500">
                                            Translated: {prettyTime(selected.translatedAt)}
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </section>

                <footer className="mt-10 text-center text-xs text-zinc-500">
                    Backend stays the same, UI keeps the wording friendly.
                </footer>
            </div>
        </main>
    );
}
