const createID = () => "id" + Math.random().toString(16).slice(2);
const utcToLocalDateTimeInputValue = (utcString) => {
    const date = new Date(utcString);
    if (isNaN(date.getTime()))
        return "";
    const pad = (n) => String(n).padStart(2, '0');
    const yyyy = date.getFullYear();
    const MM = pad(date.getMonth() + 1);
    const dd = pad(date.getDate());
    const hh = pad(date.getHours());
    const mm = pad(date.getMinutes());
    return `${yyyy}-${MM}-${dd}T${hh}:${mm}`;
};
function getUTCDateTimeFromInput(inputElement) {
    const localValue = inputElement.value; // "2025-05-31T03:00"
    if (!localValue)
        return null;
    const localDate = new Date(localValue); // interpretiert als lokale Zeit
    return localDate.toISOString(); // → "2025-05-31T01:00:00.000Z"
}
function utcToLocalDateInputValue(utcString) {
    const date = new Date(utcString);
    if (isNaN(date.getTime()))
        return "";
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
function getUTCDateFromInput(inputElement) {
    const dateOnly = inputElement.value; // z. B. "2025-05-31"
    if (!dateOnly)
        return null;
    const date = new Date(dateOnly); // wird zu 00:00 lokale Zeit
    return date.toISOString(); // → "2025-05-30T22:00:00.000Z" (wenn in CEST)
}
export { createID, utcToLocalDateTimeInputValue, getUTCDateTimeFromInput, utcToLocalDateInputValue, getUTCDateFromInput };
