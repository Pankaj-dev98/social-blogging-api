const sessionKey = "blogging-api-session";
const root = document.querySelector(".detail-shell");
const postId = root.dataset.postId;
const feedback = document.getElementById("detail-feedback");

function readSession() {
    const raw = window.localStorage.getItem(sessionKey);
    return raw ? JSON.parse(raw) : { email: "", token: "" };
}

function sessionQuery() {
    const session = readSession();
    return `email=${encodeURIComponent(session.email)}&token=${encodeURIComponent(session.token)}`;
}

async function request(url) {
    const response = await fetch(url);
    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

    if (!response.ok) {
        throw payload;
    }

    return payload;
}

function messageFrom(error) {
    if (typeof error === "string") {
        return error;
    }
    if (error?.message) {
        return error.message;
    }
    return "Unable to load this post.";
}

function showFeedback(message, type = "error") {
    feedback.textContent = message;
    feedback.className = `notice ${type}`;
    feedback.hidden = false;
}

function formatDate(value) {
    return value ? new Date(value).toLocaleString() : "";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function renderComments(comments) {
    const target = document.getElementById("comments-list");

    if (!comments.length) {
        target.innerHTML = `<div class="empty-state">No comments yet.</div>`;
        return;
    }

    target.innerHTML = comments.map(comment => `
        <article class="comment-card">
            <div class="comment-meta">${escapeHtml(comment.authorName)} · ${formatDate(comment.creationTime)}</div>
            <p>${escapeHtml(comment.commentText)}</p>
        </article>
    `).join("");
}

async function loadPost() {
    const session = readSession();
    if (!session.email || !session.token) {
        showFeedback("Please sign in to view this post.");
        return;
    }

    try {
        const post = await request(`/ui/posts/${postId}?${sessionQuery()}`);
        document.getElementById("detail-title").textContent = post.title;
        document.getElementById("detail-meta").textContent = `${post.authorName} · ${formatDate(post.creationTime)}`;
        document.getElementById("detail-body").textContent = post.postText;
        renderComments(post.comments);
    } catch (error) {
        showFeedback(messageFrom(error));
    }
}

loadPost();
