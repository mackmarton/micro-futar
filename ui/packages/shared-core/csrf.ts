// A gateway CSRF-védelme: minden állapotváltoztató /api/** kérésnek hordoznia kell ezt a fejlécet.
// A párja az api_gateway `app.csrf.*` beállítása, a kettőt együtt kell módosítani.
export const CSRF_HEADER_NAME = 'X-Requested-With';
export const CSRF_HEADER_VALUE = 'micro-futar';

// A hiányzó fejléc programhiba, nem felhasználói hiba, ezért csak a konzolra kerül.
export const reportCsrfRejection = (status: number | undefined, body: unknown, url: string | undefined) => {
  const code = (body as { code?: unknown } | null | undefined)?.code;
  if (status === 403 && code === 'CSRF_HEADER_MISSING') {
    console.error(`CSRF_HEADER_MISSING: a kérésből hiányzik a ${CSRF_HEADER_NAME} fejléc (${url ?? '?'})`);
  }
};
