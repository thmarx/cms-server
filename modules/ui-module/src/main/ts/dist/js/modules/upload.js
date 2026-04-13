import { getCSRFToken } from "./utils";
// uploadFileWithProgress.js
export function uploadFileWithProgress({ uploadEndpoint, file, uri, onProgress, onSuccess, onError }) {
    if (!file) {
        onError?.("No file selected.");
        return;
    }
    const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    if (file.size > MAX_FILE_SIZE_BYTES) {
        onError?.(`File is too large. Maximum size is ${MAX_FILE_SIZE_BYTES} bytes.`);
        return;
    }
    const formData = new FormData();
    formData.append("file", file);
    formData.append("uri", uri);
    const xhr = new XMLHttpRequest();
    xhr.open("POST", uploadEndpoint ?? "/manager/upload", true);
    xhr.setRequestHeader("X-CSRF-Token", getCSRFToken());
    xhr.upload.onprogress = (event) => {
        if (event.lengthComputable && typeof onProgress === "function") {
            const percent = Math.round((event.loaded / event.total) * 100);
            onProgress(percent);
        }
    };
    xhr.onload = () => {
        if (xhr.status === 200) {
            try {
                const json = JSON.parse(xhr.responseText);
                onSuccess?.(json); // Übergibt das JSON an den Callback
            }
            catch (e) {
                onError?.("Response is not valid JSON.");
            }
        }
        else {
            onError?.(`Upload failed: ${xhr.status} ${xhr.statusText}`);
        }
    };
    xhr.onerror = () => {
        onError?.("Upload error occurred.");
    };
    xhr.send(formData);
}
