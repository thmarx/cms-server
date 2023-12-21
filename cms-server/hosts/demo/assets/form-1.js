// form.js
const characters ='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
const generateString = (length) => {
    let result = ''
    const charactersLength = characters.length
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength))
    }

    return result;
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelector("#reloadCaptcha").addEventListener("click", () => {
        let href = new URL(document.getElementById("captchaImg").src)
        let key = generateString(8)
        href.searchParams.set('key', key)

        document.getElementById("captchaKey").value = key
        document.getElementById("captchaImg").src = href.toString()
    })
})
