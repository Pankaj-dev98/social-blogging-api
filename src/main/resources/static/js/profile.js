const sessionKey = "blogging-api-session";
const feedback = document.getElementById("profile-feedback");
const profileContent = document.getElementById("profile-content");

function readSession() {
    const raw = window.localStorage.getItem(sessionKey);
    return raw ? JSON.parse(raw) : { email: "", token: "" };
}

function clearSession() {
    window.localStorage.removeItem(sessionKey);
}

function sessionQuery() {
    const session = readSession();
    return `email=${encodeURIComponent(session.email)}&token=${encodeURIComponent(session.token)}`;
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

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
    return "Unable to load your profile.";
}

function showFeedback(message, type = "error") {
    feedback.textContent = message;
    feedback.className = `notice ${type}`;
    feedback.hidden = false;
}

function formatDate(value) {
    return value ? new Date(value).toLocaleString() : "";
}

function excerpt(text) {
    if (!text) {
        return "";
    }
    return text.length > 180 ? `${text.slice(0, 180)}...` : text;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function renderPostList(posts) {
    const target = document.getElementById("profile-posts-list");

    if (!posts.length) {
        target.innerHTML = `<div class="empty-state">You have not published any posts yet.</div>`;
        return;
    }

    target.innerHTML = posts.map(post => `
        <a class="post-card" href="/post/${post.id}">
            <div class="post-meta">${formatDate(post.creationTime)}</div>
            <h3>${escapeHtml(post.title)}</h3>
            <p>${escapeHtml(excerpt(post.postText))}</p>
            <div class="post-meta">${post.commentCount} comments</div>
        </a>
    `).join("");
}

function renderProfile(data) {
    document.getElementById("profile-name").textContent = data.account.name;
    document.getElementById("profile-subtitle").textContent = `${data.account.postCount} published posts`;
    document.getElementById("profile-email").textContent = data.account.email;
    document.getElementById("profile-post-count").textContent = data.account.postCount;
    document.getElementById("profile-follower-count").textContent = data.account.followerCount;
    renderPostList(data.posts);
    profileContent.hidden = false;
}

async function loadProfile() {
    const session = readSession();
    if (!session.email || !session.token) {
        window.location.href = "/";
        return;
    }

    try {
        const data = await request(`/ui/me/posts?${sessionQuery()}`);
        renderProfile(data);
    } catch (error) {
        clearSession();
        showFeedback(messageFrom(error));
    }
}

document.getElementById("refresh-profile-posts").addEventListener("click", async () => {
    try {
        await loadProfile();
    } catch (error) {
        showFeedback(messageFrom(error));
    }
});

document.getElementById("profile-signout-button").addEventListener("click", async () => {
    const session = readSession();

    try {
        if (session.email && session.token) {
            await request(`/users/signout?email=${encodeURIComponent(session.email)}&token=${encodeURIComponent(session.token)}`, {
                method: "DELETE"
            });
        }
    } catch (error) {
        // Local sign-out should still work if the token was already invalidated.
    }

    clearSession();
    window.location.href = "/";
});

loadProfile();
