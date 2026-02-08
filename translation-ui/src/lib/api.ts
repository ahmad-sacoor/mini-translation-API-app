export const API_URL =
    process.env.NEXT_PUBLIC_API_URL?.trim() || "http://localhost:8080";

export type Status = "CREATED" | "TRANSLATED" | "FAILED";

export type TranslationRecord = {
    id: number;
    originalText: string;
    sourceLang: string;
    targetLang: string;
    status: Status;
    translatedText: string | null;
    createdAt: string;
    translatedAt: string | null;
};

type BackendError = { error?: string; message?: string };

async function readNiceError(res: Response): Promise<string> {
    const text = await res.text();
    try {
        const data = JSON.parse(text) as BackendError;
        if (data?.message) return data.message;
        if (data?.error) return data.error;
    } catch {
        // ignore
    }
    return text || `Request failed (${res.status})`;
}

export async function createRecord(input: {
    originalText: string;
    sourceLang: string;
    targetLang: string;
}): Promise<TranslationRecord> {
    const res = await fetch(`${API_URL}/tickets`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(input),
    });

    if (!res.ok) throw new Error(await readNiceError(res));
    return res.json();
}

export async function translateRecord(id: number): Promise<TranslationRecord> {
    const res = await fetch(`${API_URL}/tickets/${id}/translate`, {
        method: "POST",
    });

    if (!res.ok) throw new Error(await readNiceError(res));
    return res.json();
}

export async function listRecords(): Promise<TranslationRecord[]> {
    const res = await fetch(`${API_URL}/tickets`, { cache: "no-store" });

    if (!res.ok) throw new Error(await readNiceError(res));
    return res.json();
}
