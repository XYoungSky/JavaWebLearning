document.addEventListener("DOMContentLoaded", () => {
    const button = document.getElementById("refresh-time")
    const output = document.getElementById("server-time")
    if (!button || !output) return

    button.addEventListener("click", () => {
        const xhr = new XMLHttpRequest()
        output.textContent = "正在请求……"
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) return
            output.textContent = xhr.status === 200
                ? xhr.responseText
                : `请求失败，HTTP ${xhr.status}`
        }
        xhr.open("GET", button.dataset.url, true)
        xhr.send()
    })
})
