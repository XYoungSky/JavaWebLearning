document.addEventListener("DOMContentLoaded", () => {
    document.querySelector("[data-error-summary]")?.focus()

    document.querySelectorAll("form[data-submit-once]").forEach(form => {
        form.addEventListener("submit", event => {
            if (event.defaultPrevented) return
            if (form.dataset.submitting === "true") {
                event.preventDefault()
                return
            }
            form.dataset.submitting = "true"
            form.setAttribute("aria-busy", "true")
            form.querySelectorAll("button[type='submit']").forEach(button => {
                button.dataset.originalText = button.textContent
                button.disabled = true
                button.textContent = "正在提交…"
            })
        })
    })

    // Browser back/forward cache can restore disabled submit buttons.
    window.addEventListener("pageshow", () => {
        document.querySelectorAll("form[data-submit-once]").forEach(form => {
            delete form.dataset.submitting
            form.removeAttribute("aria-busy")
            form.querySelectorAll("button[data-original-text]").forEach(button => {
                button.disabled = false
                button.textContent = button.dataset.originalText
                delete button.dataset.originalText
            })
        })
    })

    const button = document.getElementById("refresh-time")
    const output = document.getElementById("server-time")
    if (!button || !output) return
    button.addEventListener("click", () => {
        const xhr = new XMLHttpRequest()
        button.disabled = true
        output.textContent = "正在请求…"
        output.setAttribute("aria-busy", "true")
        xhr.open("GET", button.dataset.url, true)
        xhr.timeout = 8000
        xhr.onload = () => {
            output.textContent = xhr.status === 200 ? xhr.responseText : `请求失败，HTTP ${xhr.status}，请重试。`
        }
        xhr.onerror = () => { output.textContent = "网络连接失败，请检查服务是否启动后重试。" }
        xhr.ontimeout = () => { output.textContent = "请求超时，请重试。" }
        xhr.onloadend = () => {
            button.disabled = false
            output.removeAttribute("aria-busy")
        }
        xhr.send()
    })
})
