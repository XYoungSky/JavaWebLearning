const arr = [0, 2, 1, 5, 9, 3, 4, 6, 7, 8]

for (let i = 0; i < arr.length; i++) {
    for (let j = 0; j < arr.length - 1; j++) {
        if (arr[j] > arr[j + 1]) {
            const tmp = arr[j]
            arr[j] = arr[j + 1]
            arr[j + 1] = tmp
        }
    }
}

console.info("页面加载时完成的冒泡排序：", arr)

function showOutput(elementId, content) {
    document.getElementById(elementId).textContent = content
}

function runTypeDemo() {
    let value = 10
    const results = [`value = ${value}，类型：${typeof value}`]

    value++
    results.push(`value++ = ${value}，类型：${typeof value}`)

    value = "Hello World"
    results.push(`重新赋值后：${value}，类型：${typeof value}`)
    results.push(`undefined：${typeof undefined}；null：${typeof null}；NaN：${typeof NaN}`)

    console.info(results)
    showOutput("syntax-output", results.join("\n"))
}

function runSortDemo() {
    const numbers = [0, 2, 1, 5, 9, 3, 4, 6, 7, 8]

    for (let i = 0; i < numbers.length; i++) {
        for (let j = 0; j < numbers.length - 1; j++) {
            if (numbers[j] > numbers[j + 1]) {
                const tmp = numbers[j]
                numbers[j] = numbers[j + 1]
                numbers[j + 1] = tmp
            }
        }
    }

    console.info(numbers)
    showOutput("syntax-output", `排序结果：[${numbers.join(", ")}]`)
}

function runControlDemo() {
    const score = Number(document.getElementById("score-input").value)

    if (Number.isNaN(score) || score < 0 || score > 100) {
        showOutput("control-output", "请输入 0～100 之间的有效数字")
        return
    }

    const equality = `'10' == 10：${"10" == 10}；'10' === 10：${"10" === 10}`
    const truthy = `!0：${!0}；!空字符串：${!""}`
    const ternary = score >= 60 ? "及格" : "不及格"

    let level
    switch (true) {
        case score >= 90:
            level = "优秀"
            break
        case score >= 60:
            level = "合格"
            break
        default:
            level = "需要继续努力"
    }

    const loopValues = []
    let index = 0
    while (index < 3) {
        loopValues.push(index)
        index++
    }

    for (let i = 3; i < 6; i++) {
        loopValues.push(i)
    }

    showOutput("control-output", `${equality}\n${truthy}\n三元运算：${ternary}；switch：${level}\n循环结果：[${loopValues.join(", ")}]`)
}

function greet(name = "同学") {
    return `你好，${name}`
}

function runCallback(callback) {
    callback("这个是回调参数")
}

function runFunctionDemo() {
    const inputName = document.getElementById("function-name").value.trim()
    const message = inputName === "" ? greet() : greet(inputName)
    console.info(message)
    showOutput("function-output", `${message}\n函数返回值的类型：${typeof message}`)
}

function runCallbackDemo() {
    runCallback(param => {
        console.info("接收到回调参数：", param)
        showOutput("function-output", `箭头函数接收到：${param}`)
    })
}

const demoStack = [1, "文本", false]

function showStack(message = "当前数组") {
    showOutput("data-output", `${message}：[${demoStack.map(item => JSON.stringify(item)).join(", ")}]`)
}

function pushArrayValue() {
    const input = document.getElementById("array-value")
    const value = input.value.trim()

    if (value === "") {
        showStack("请先输入内容，当前数组")
        return
    }

    demoStack.push(value)
    input.value = ""
    showStack("push 后")
}

function popArrayValue() {
    const removed = demoStack.pop()
    showStack(`pop 取出 ${JSON.stringify(removed)}，剩余数组`)
}

function runObjectDemo() {
    const student = {
        name: "小明",
        introduce() {
            return `我是${this.name}`
        }
    }

    student.role = "学生"
    showOutput("data-output", `${student.introduce()}，动态添加的 role 属性为“${student.role}”`)
}

function checkIllegal(element) {
    const hint = document.getElementById("password-hint")
    const isValid = element.value.length >= 6

    element.classList.toggle("illegal-pwd", !isValid)
    hint.classList.toggle("is-error", !isValid)
    hint.classList.toggle("is-valid", isValid)

    if (element.value.length === 0) {
        hint.textContent = "密码不能为空，至少需要 6 位"
    } else if (!isValid) {
        hint.textContent = `当前 ${element.value.length} 位，还需要 ${6 - element.value.length} 位`
    } else {
        hint.textContent = "密码长度符合要求"
    }
}

function sendRequest() {
    const output = document.getElementById("xhr-output")
    const xhr = new XMLHttpRequest()

    output.textContent = "请求发送中……"
    xhr.open("GET", "README.md")

    xhr.onload = () => {
        const preview = xhr.responseText.replace(/\s+/g, " ").trim().slice(0, 100)
        output.textContent = `请求完成，HTTP 状态码：${xhr.status}\n响应内容预览：${preview || "（响应为空）"}`
    }

    xhr.onerror = () => {
        output.textContent = "请求失败。请通过本地 HTTP 服务器打开本页面后重试。"
    }

    xhr.send()
}

document.getElementById("run-types").onclick = runTypeDemo
document.getElementById("run-sort").onclick = runSortDemo
document.getElementById("run-control").onclick = runControlDemo
document.getElementById("run-function").onclick = runFunctionDemo
document.getElementById("run-callback").onclick = runCallbackDemo
document.getElementById("array-push").onclick = pushArrayValue
document.getElementById("array-pop").onclick = popArrayValue
document.getElementById("run-object").onclick = runObjectDemo
document.getElementById("send-xhr").onclick = sendRequest

document.getElementById("event-button").onclick = () => {
    showOutput("event-output", "onclick 事件已触发")
}

document.getElementById("event-form").onsubmit = event => {
    event.preventDefault()
    showOutput("event-output", "onsubmit 事件已触发；preventDefault() 阻止了页面刷新")
}
