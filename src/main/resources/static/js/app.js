const sessionKey = "blogging-api-session";

const signedOutView = document.getElementById("signed-out-view");
const signedInView = document.getElementById("signed-in-view");
const welcomeText = document.getElementById("welcome-text");
const appFeedback = document.getElementById("app-feedback");

function readSession() {
    const raw = window.localStorage.getItem(sessionKey);
    return raw ? JSON.parse(raw) : { email: "", token: "" };
}

function writeSession(email, token) {
    window.localStorage.setItem(sessionKey, JSON.stringify({ email, token }));
}

function clearSession() {
    window.localStorage.removeItem(sessionKey);
}

function sessionQuery() {
    const session = readSession();
    return `email=${encodeURIComponent(session.email)}&token=${encodeURIComponent(session.token)}`;
}

function requireSession() {
    const session = readSession();
    if (!session.email || !session.token) {
        throw new Error("Please sign in first.");
    }
    return session;
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
    if (!error) {
        return "Something went wrong.";
    }
    if (typeof error === "string") {
        return error;
    }
    if (error.message) {
        return error.message;
    }
    return Object.entries(error)
        .map(([key, value]) => `${key}: ${value}`)
        .join(" ");
}

function setInlineFeedback(id, message, type) {
    const element = document.getElementById(id);
    element.textContent = message;
    element.className = `feedback ${type}`;
}

function showAppFeedback(message, type = "success") {
    appFeedback.textContent = message;
    appFeedback.className = `notice ${type}`;
    appFeedback.hidden = false;
}

function showSignedOut() {
    signedOutView.hidden = false;
    signedInView.hidden = true;
}

function showSignedIn(account) {
    signedOutView.hidden = true;
    signedInView.hidden = false;
    welcomeText.textContent = `Signed in as ${account.name}`;
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

function renderPostList(targetId, posts) {
    const target = document.getElementById(targetId);

    if (!posts.length) {
        target.innerHTML = `<div class="empty-state">No posts to show.</div>`;
        return;
    }

    target.innerHTML = posts.map(post => `
        <a class="post-card" href="/post/${post.id}">
            <div class="post-meta">${escapeHtml(post.authorName)} · ${formatDate(post.creationTime)}</div>
            <h3>${escapeHtml(post.title)}</h3>
            <p>${escapeHtml(excerpt(post.postText))}</p>
            <div class="post-meta">${post.commentCount} comments</div>
        </a>
    `).join("");
}

function renderAccounts(targetId, accounts) {
    const target = document.getElementById(targetId);

    if (!accounts.length) {
        target.innerHTML = `<div class="empty-state">No accounts found.</div>`;
        return;
    }

    target.innerHTML = accounts.map(account => `
        <div class="account-card">
            <div>
                <h3>${escapeHtml(account.name)}</h3>
                <div class="account-meta">${account.postCount} posts · ${account.followerCount} followers</div>
            </div>
            <div class="card-actions">
                <button type="button" class="secondary" data-view-posts="${account.id}">View posts</button>
                <button type="button" data-follow="${account.id}" ${account.following ? "disabled" : ""}>
                    ${account.following ? "Following" : "Follow"}
                </button>
            </div>
        </div>
    `).join("");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function loadMe() {
    const account = await request(`/ui/me?${sessionQuery()}`);
    showSignedIn(account);
    return account;
}

async function loadFeed() {
    const posts = await request(`/ui/feed?${sessionQuery()}`);
    renderPostList("feed-list", posts);
}

async function loadFollowers() {
    const followers = await request(`/ui/followers?${sessionQuery()}`);
    document.getElementById("followers-panel").hidden = false;
    renderAccounts("followers-list", followers);
}

async function loadAccountPosts(accountId) {
    const data = await request(`/ui/accounts/${accountId}/posts?${sessionQuery()}`);
    document.getElementById("account-posts-title").textContent = `${data.account.name}'s posts`;
    renderPostList("account-posts-list", data.posts);
}

async function followAccount(accountId) {
    const session = requireSession();
    await request(`/ui/accounts/${accountId}/follow`, {
        method: "POST",
        body: JSON.stringify(session)
    });
    showAppFeedback("Account followed.");
    await loadFeed();
}

document.getElementById("signup-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.target);

    try {
        await request("/users/signup", {
            method: "POST",
            body: JSON.stringify({
                name: form.get("name"),
                email: form.get("email"),
                password: form.get("password"),
                dob: form.get("dob"),
                gender: form.get("gender")
            })
        });
        setInlineFeedback("signup-feedback", "Account created. You can sign in now.", "success");
        event.target.reset();
    } catch (error) {
        setInlineFeedback("signup-feedback", messageFrom(error), "error");
    }
});

document.getElementById("signin-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.target);

    try {
        const data = await request("/users/signin", {
            method: "POST",
            body: JSON.stringify({
                email: form.get("email"),
                password: form.get("password")
            })
        });

        const token = String(data).split("Token = ")[1] || "";
        if (!token) {
            setInlineFeedback("signin-feedback", String(data), "error");
            return;
        }

        writeSession(String(form.get("email")), token);
        await initializeSignedInView();
    } catch (error) {
        setInlineFeedback("signin-feedback", messageFrom(error), "error");
    }
});

document.getElementById("signout-button").addEventListener("click", async () => {
    try {
        const session = requireSession();
        await request(`/users/signout?email=${encodeURIComponent(session.email)}&token=${encodeURIComponent(session.token)}`, {
            method: "DELETE"
        });
    } catch (error) {
        // Local sign-out still clears the browser session if the server token is already gone.
    }

    clearSession();
    showSignedOut();
});

document.getElementById("new-post-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.target);

    try {
        await request("/posts/", {
            method: "POST",
            body: JSON.stringify({
                authDto: requireSession(),
                title: form.get("title"),
                blogText: form.get("blogText")
            })
        });
        event.target.reset();
        showAppFeedback("Post published.");
        await loadFeed();
    } catch (error) {
        showAppFeedback(messageFrom(error), "error");
    }
});

document.getElementById("refresh-feed").addEventListener("click", async () => {
    try {
        await loadFeed();
    } catch (error) {
        showAppFeedback(messageFrom(error), "error");
    }
});

document.getElementById("followers-link").addEventListener("click", async () => {
    try {
        await loadFollowers();
    } catch (error) {
        showAppFeedback(messageFrom(error), "error");
    }
});

document.getElementById("account-search-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = new FormData(event.target);

    try {
        const accounts = await request(`/ui/accounts/search?${sessionQuery()}&name=${encodeURIComponent(form.get("name"))}`);
        renderAccounts("account-search-results", accounts);
    } catch (error) {
        showAppFeedback(messageFrom(error), "error");
    }
});

document.addEventListener("click", async (event) => {
    const viewPostsButton = event.target.closest("[data-view-posts]");
    const followButton = event.target.closest("[data-follow]");

    try {
        if (viewPostsButton) {
            await loadAccountPosts(viewPostsButton.dataset.viewPosts);
        }

        if (followButton) {
            await followAccount(followButton.dataset.follow);
            followButton.textContent = "Following";
            followButton.disabled = true;
        }
    } catch (error) {
        showAppFeedback(messageFrom(error), "error");
    }
});

async function initializeSignedInView() {
    try {
        const account = await loadMe();
        showSignedIn(account);
        await loadFeed();
    } catch (error) {
        clearSession();
        showSignedOut();
    }
}

if (readSession().token) {
    initializeSignedInView();
} else {
    showSignedOut();
}
