const ui = {
    state: "login"
};
const setWarningMessage = (message) => {
    const alert = document.querySelector("#loginMessage");
    if (alert) {
        alert.innerHTML = message;
        alert.classList.add("alert-warning");
    }
};
const clearMessage = () => {
    const alert = document.querySelector("#loginMessage");
    if (alert) {
        alert.innerHTML = "";
        alert.classList.remove("alert-warning");
    }
};
const signIn = (e) => {
    e.preventDefault();
    clearMessage();
    fetch(window.ui.login_url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            command: "login",
            data: {
                username: document.querySelector("#inputUsername")?.value,
                password: document.querySelector("#inputPassword")?.value
            }
        })
    })
        .then(response => response.json())
        .then(result => {
        if (result.status === "2fa_required") {
            document.querySelector("#signIn")?.classList.toggle("hidden");
            document.querySelector("#validate")?.classList.toggle("hidden");
            ui.state = "validate";
        }
        else if (result.status === "ok") {
            window.location.href = result.redirect || window.ui.manager_url;
        }
        else {
            setWarningMessage("Login failed, maybe your credentials are incorrect. Please try again or contact your admin.");
        }
    });
};
const validate = (e) => {
    e.preventDefault();
    clearMessage();
    // 2FA-Feld anzeigen oder ist schon sichtbar
    const code = document.querySelector("#inputCode")?.value;
    fetch(window.ui.login_url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            command: "validate",
            data: {
                code: code
            }
        })
    })
        .then(response => response.json())
        .then(validateResult => {
        if (validateResult.status === "ok") {
            window.location.href = validateResult.redirect || window.ui.manager_url;
        }
        else {
            setWarningMessage("Validation of the login code failed. Please try again or contact your admin.");
        }
    });
};
const formSubmit = (e) => {
    e.preventDefault();
    if (ui.state === "login") {
        signIn(e);
    }
    else {
        validate(e);
    }
};
if (document.querySelector("#resetButton")) {
    document.querySelector("#resetButton")?.addEventListener("click", (e) => {
        ui.state = "login";
        document.querySelector("#loginForm").reset();
        document.querySelector("#signIn")?.classList.remove("hidden");
        document.querySelector("#validate")?.classList.add("hidden");
    });
}
if (document.querySelector("#validateButton")) {
    document.querySelector("#validateButton")?.addEventListener("click", validate);
}
document.querySelector("#signInButton")?.addEventListener("click", signIn);
document.querySelector("#loginForm")?.addEventListener("submit", formSubmit);
