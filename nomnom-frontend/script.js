// ============================================================
// NOMNOM CUSTOMER SERVICE
// Talks to the Spring Boot Customer Service API, which persists
// to the H2 database (see application.properties: jdbc:h2:mem:nomnom_customer).
// ============================================================

const CUSTOMER_API = "http://localhost:8080/customers";

const LAST_CODE_KEY = "nomnom.lastCustomerCode";

// Stores the currently loaded customer
let currentCustomer = null;


// ============================================================
// PAGE NAVIGATION
// ============================================================

function showPage(page) {

    const registerPage = document.getElementById("registerPage");
    const profilePage = document.getElementById("profilePage");

    const registerNav = document.getElementById("registerNav");
    const profileNav = document.getElementById("profileNav");

    if (page === "register") {

        registerPage.classList.add("active-page");
        profilePage.classList.remove("active-page");

        registerNav.classList.add("active");
        profileNav.classList.remove("active");

    } else {

        profilePage.classList.add("active-page");
        registerPage.classList.remove("active-page");

        profileNav.classList.add("active");
        registerNav.classList.remove("active");

        // If we land on the profile page without anything loaded yet,
        // try to restore the last customer that was viewed on this device.
        if (!currentCustomer) {
            const savedCode = localStorage.getItem(LAST_CODE_KEY);

            if (savedCode) {
                document.getElementById("lookupCode").value = savedCode;
                loadCustomerByCode(savedCode);
            }
        }
    }

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


// ============================================================
// TOASTS
// ============================================================

function showToast(message, type = "info") {

    const container = document.getElementById("toastContainer");

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    // Trigger enter animation on next frame
    requestAnimationFrame(() => toast.classList.add("toast-visible"));

    setTimeout(() => {
        toast.classList.remove("toast-visible");
        setTimeout(() => toast.remove(), 250);
    }, 4200);
}


// ============================================================
// API HELPER
// ============================================================

// Wraps fetch so callers always get a parsed JSON body, whether the
// response is a success or one of the JSON error bodies produced by
// GlobalExceptionHandler on the backend.
async function apiRequest(path, options = {}) {

    let response;

    try {
        response = await fetch(`${CUSTOMER_API}${path}`, {
            headers: { "Content-Type": "application/json" },
            ...options
        });
    } catch (networkError) {
        throw new Error(
            "Could not reach Customer Service. Make sure the backend " +
            "is running on http://localhost:8080."
        );
    }

    // 204 No Content (e.g. DELETE) has no body to parse
    if (response.status === 204) {
        return { ok: true, data: null };
    }

    let data = null;

    try {
        data = await response.json();
    } catch {
        // Non-JSON body (shouldn't normally happen now that errors are JSON)
        data = null;
    }

    if (!response.ok) {
        const error = new Error(
            (data && data.message) || "Something went wrong. Please try again."
        );
        error.fieldErrors = data && data.fieldErrors;
        error.status = response.status;
        throw error;
    }

    return { ok: true, data };
}


// ============================================================
// FIELD-LEVEL VALIDATION ERRORS
// ============================================================

function clearFieldErrors(prefix = "err-") {
    document.querySelectorAll(`[id^="${prefix}"]`).forEach(el => {
        el.textContent = "";
    });
}

function applyFieldErrors(fieldErrors, prefix = "err-") {
    if (!fieldErrors) return;

    Object.entries(fieldErrors).forEach(([field, message]) => {
        const el = document.getElementById(`${prefix}${field}`);
        if (el) el.textContent = message;
    });
}


// ============================================================
// REGISTER CUSTOMER
// ============================================================

document
    .getElementById("registerForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        clearFieldErrors();

        const customerData = {
            fullName: document.getElementById("fullName").value.trim(),
            email: document.getElementById("email").value.trim() || null,
            phoneNumber: document.getElementById("phoneNumber").value.trim() || null,
            password: document.getElementById("password").value,
            dateOfBirth: document.getElementById("dateOfBirth").value || null,
            address: {
                unitOrHouseNumber: document.getElementById("unitOrHouseNumber").value.trim(),
                street: document.getElementById("street").value.trim(),
                postcode: document.getElementById("postcode").value.trim(),
                city: document.getElementById("city").value.trim(),
                state: document.getElementById("state").value.trim(),
                country: document.getElementById("country").value.trim()
            }
        };

        const submitButton = document.getElementById("registerSubmitButton");
        const buttonLabel = submitButton.querySelector(".button-label");
        const originalLabel = buttonLabel.textContent;

        submitButton.disabled = true;
        buttonLabel.textContent = "Creating Account...";

        try {

            const { data } = await apiRequest("", {
                method: "POST",
                body: JSON.stringify(customerData)
            });

            currentCustomer = data;
            localStorage.setItem(LAST_CODE_KEY, data.customerCode);

            displayCustomerProfile(data);

            showToast(
                `Account created! Your customer code is ${data.customerCode}.`,
                "success"
            );

            showPage("profile");

            document.getElementById("registerForm").reset();

        } catch (error) {

            console.error("Customer registration failed:", error);

            if (error.fieldErrors) {
                applyFieldErrors(error.fieldErrors);
            }

            showToast(error.message, "error");

        } finally {

            submitButton.disabled = false;
            buttonLabel.textContent = originalLabel;
        }
    });


// ============================================================
// LOOKUP / LOAD CUSTOMER BY CODE
// ============================================================

document
    .getElementById("lookupToggleLink")
    .addEventListener("click", function (event) {
        event.preventDefault();
        document.getElementById("lookupBar").hidden = false;
        document.getElementById("lookupToggle").hidden = true;
        document.getElementById("lookupCode").focus();
    });


document
    .getElementById("lookupButton")
    .addEventListener("click", function () {

        const code = document.getElementById("lookupCode").value.trim();

        if (!code) {
            showToast("Enter a customer code to look up.", "error");
            return;
        }

        loadCustomerByCode(code);
    });


document
    .getElementById("lookupCode")
    .addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            document.getElementById("lookupButton").click();
        }
    });


async function loadCustomerByCode(customerCode) {

    const lookupButton = document.getElementById("lookupButton");
    const originalText = lookupButton.textContent;

    lookupButton.disabled = true;
    lookupButton.textContent = "Searching...";

    try {

        const { data } = await apiRequest(`/${encodeURIComponent(customerCode)}`);

        currentCustomer = data;
        localStorage.setItem(LAST_CODE_KEY, data.customerCode);

        displayCustomerProfile(data);
        showToast(`Loaded account ${data.customerCode}.`, "success");

    } catch (error) {

        console.error("Customer lookup failed:", error);
        showToast(error.message, "error");

    } finally {

        lookupButton.disabled = false;
        lookupButton.textContent = originalText;
    }
}


// ============================================================
// DISPLAY CUSTOMER PROFILE
// ============================================================

function displayCustomerProfile(customer) {

    document.getElementById("profileEmptyState").hidden = true;

    const card = document.getElementById("profileCard");
    card.hidden = false;

    document.getElementById("profileName").textContent =
        customer.fullName || "Customer Name";

    document.getElementById("profileAvatar").textContent =
        (customer.fullName || "N").trim().charAt(0).toUpperCase();

    document.getElementById("profileCustomerCode").textContent =
        customer.customerCode || "—";

    document.getElementById("profileStatus").textContent =
        customer.accountStatus || "ACTIVE";

    document.getElementById("profileEmail").textContent =
        customer.email || "—";

    document.getElementById("profilePhone").textContent =
        customer.phoneNumber || "—";

    document.getElementById("profilePoints").textContent =
        customer.loyaltyPoints ?? 0;

    document.getElementById("profileDate").textContent =
        formatDate(customer.dateRegistered);

    document.getElementById("profileAddress").textContent =
        formatAddress(customer.address);

    exitEditMode();
}


function formatDate(date) {

    if (!date) return "—";

    if (typeof date === "string") {
        return date.split("T")[0];
    }

    return date;
}


function formatAddress(address) {

    if (!address) return "—";

    const parts = [
        address.unitOrHouseNumber,
        address.street,
        address.postcode,
        address.city,
        address.state,
        address.country
    ].filter(Boolean);

    return parts.length ? parts.join(", ") : "—";
}


function clearCustomerProfile() {

    document.getElementById("profileCard").hidden = true;
    document.getElementById("profileEmptyState").hidden = false;
}


// ============================================================
// EDIT PROFILE
// ============================================================

document
    .getElementById("editButton")
    .addEventListener("click", function () {

        if (!currentCustomer) {
            showToast("Load an account before editing it.", "error");
            return;
        }

        enterEditMode();
    });


document
    .getElementById("cancelEditButton")
    .addEventListener("click", function () {
        exitEditMode();
    });


function enterEditMode() {

    const c = currentCustomer;

    document.getElementById("editFullName").value = c.fullName || "";
    document.getElementById("editEmail").value = c.email || "";
    document.getElementById("editPhoneNumber").value = c.phoneNumber || "";
    document.getElementById("editDateOfBirth").value =
        c.dateOfBirth ? String(c.dateOfBirth).split("T")[0] : "";

    const address = c.address || {};
    document.getElementById("editUnitOrHouseNumber").value = address.unitOrHouseNumber || "";
    document.getElementById("editStreet").value = address.street || "";
    document.getElementById("editPostcode").value = address.postcode || "";
    document.getElementById("editCity").value = address.city || "";
    document.getElementById("editState").value = address.state || "";
    document.getElementById("editCountry").value = address.country || "";

    document.getElementById("profileViewDetails").hidden = true;
    document.getElementById("profileEditForm").hidden = false;
    document.getElementById("editButton").hidden = true;
}


function exitEditMode() {

    document.getElementById("profileViewDetails").hidden = false;
    document.getElementById("profileEditForm").hidden = true;
    document.getElementById("editButton").hidden = false;
}


document
    .getElementById("profileEditForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        if (!currentCustomer) return;

        const updateData = {
            fullName: document.getElementById("editFullName").value.trim(),
            email: document.getElementById("editEmail").value.trim() || null,
            phoneNumber: document.getElementById("editPhoneNumber").value.trim() || null,
            dateOfBirth: document.getElementById("editDateOfBirth").value || null,
            address: {
                unitOrHouseNumber: document.getElementById("editUnitOrHouseNumber").value.trim(),
                street: document.getElementById("editStreet").value.trim(),
                postcode: document.getElementById("editPostcode").value.trim(),
                city: document.getElementById("editCity").value.trim(),
                state: document.getElementById("editState").value.trim(),
                country: document.getElementById("editCountry").value.trim()
            }
        };

        const saveButton = document.getElementById("saveProfileButton");
        const label = saveButton.querySelector(".button-label");
        const originalLabel = label.textContent;

        saveButton.disabled = true;
        label.textContent = "Saving...";

        try {

            const { data } = await apiRequest(
                `/${encodeURIComponent(currentCustomer.customerCode)}`,
                {
                    method: "PUT",
                    body: JSON.stringify(updateData)
                }
            );

            currentCustomer = data;
            displayCustomerProfile(data);

            showToast("Profile updated.", "success");

        } catch (error) {

            console.error("Profile update failed:", error);
            showToast(error.message, "error");

        } finally {

            saveButton.disabled = false;
            label.textContent = originalLabel;
        }
    });


// ============================================================
// DELETE CUSTOMER
// ============================================================

document
    .getElementById("deleteButton")
    .addEventListener("click", async function () {

        if (!currentCustomer || !currentCustomer.customerCode) {
            showToast("No account is loaded to delete.", "error");
            return;
        }

        const confirmDelete = confirm(
            "Are you sure you want to delete this account?"
        );

        if (!confirmDelete) return;

        try {

            await apiRequest(
                `/${encodeURIComponent(currentCustomer.customerCode)}`,
                { method: "DELETE" }
            );

            showToast("Customer account deleted.", "success");

            currentCustomer = null;
            localStorage.removeItem(LAST_CODE_KEY);
            document.getElementById("lookupCode").value = "";

            clearCustomerProfile();

            showPage("register");

        } catch (error) {

            console.error("Customer deletion failed:", error);
            showToast(error.message, "error");
        }
    });


// ============================================================
// RESTAURANT & CATALOG SERVICE
// Student 2 — Restaurant & Catalog Service
// Talks to the Spring Boot Restaurant Service API on port 8081.
// API base: http://localhost:8081/restaurants
// ============================================================

const RESTAURANT_API = "http://localhost:8081/restaurants";

// Tracks the restaurant currently being managed in the Manage tab
let currentRestaurant = null;


// ------------------------------------------------------------
// PAGE NAVIGATION — extend the existing showPage() function
// to also handle the two new restaurant pages
// ------------------------------------------------------------

const _originalShowPage = showPage;

showPage = function (page) {

    const restaurantsPage   = document.getElementById("restaurantsPage");
    const addRestaurantPage = document.getElementById("addRestaurantPage");

    const restaurantsNav    = document.getElementById("restaurantsNav");
    const addRestaurantNav  = document.getElementById("addRestaurantNav");

    // Deactivate restaurant pages/nav first
    [restaurantsPage, addRestaurantPage].forEach(el => el && el.classList.remove("active-page"));
    [restaurantsNav, addRestaurantNav].forEach(el => el && el.classList.remove("active"));

    if (page === "restaurants") {

        restaurantsPage.classList.add("active-page");
        restaurantsNav.classList.add("active");

        // Also deactivate the customer pages
        document.getElementById("registerPage").classList.remove("active-page");
        document.getElementById("profilePage").classList.remove("active-page");
        document.getElementById("registerNav").classList.remove("active");
        document.getElementById("profileNav").classList.remove("active");

        window.scrollTo({ top: 0, behavior: "smooth" });

    } else if (page === "addRestaurant") {

        addRestaurantPage.classList.add("active-page");
        addRestaurantNav.classList.add("active");

        document.getElementById("registerPage").classList.remove("active-page");
        document.getElementById("profilePage").classList.remove("active-page");
        document.getElementById("registerNav").classList.remove("active");
        document.getElementById("profileNav").classList.remove("active");

        window.scrollTo({ top: 0, behavior: "smooth" });

    } else {
        // Fall through to the original customer-service navigation
        _originalShowPage(page);
    }
};


// ------------------------------------------------------------
// RESTAURANT API HELPER
// Same pattern as apiRequest() above, just pointed at port 8081
// ------------------------------------------------------------

async function rstApiRequest(path, options = {}) {

    let response;

    try {
        response = await fetch(`${RESTAURANT_API}${path}`, {
            headers: { "Content-Type": "application/json" },
            ...options
        });
    } catch (networkError) {
        throw new Error(
            "Could not reach Restaurant Service. Make sure it is running on http://localhost:8081."
        );
    }

    if (response.status === 204) {
        return { ok: true, data: null };
    }

    let data = null;

    try {
        data = await response.json();
    } catch {
        data = null;
    }

    if (!response.ok) {
        const error = new Error(
            (data && data.message) || "Something went wrong. Please try again."
        );
        error.fieldErrors = data && data.fieldErrors;
        error.status = response.status;
        throw error;
    }

    return { ok: true, data };
}


// ------------------------------------------------------------
// BROWSE RESTAURANTS PAGE — Load All
// ------------------------------------------------------------

document.getElementById("rstLoadAllButton")
    .addEventListener("click", async function () {

        const btn = this;
        btn.disabled = true;
        btn.textContent = "Loading...";

        try {
            const { data } = await rstApiRequest("");
            renderRestaurantList(data, document.getElementById("rstListContainer"));
        } catch (error) {
            showToast(error.message, "error");
        } finally {
            btn.disabled = false;
            btn.textContent = "Load All Restaurants";
        }
    });


// Browse page — lookup by code
document.getElementById("rstLookupButton")
    .addEventListener("click", async function () {

        const code = document.getElementById("rstLookupCode").value.trim();

        if (!code) {
            showToast("Enter a restaurant code to look up.", "error");
            return;
        }

        this.disabled = true;
        this.textContent = "Searching...";

        try {
            const { data } = await rstApiRequest(`/${encodeURIComponent(code)}`);
            renderRestaurantList([data], document.getElementById("rstListContainer"));
            showToast("Restaurant loaded.", "success");
        } catch (error) {
            showToast(error.message, "error");
        } finally {
            this.disabled = false;
            this.textContent = "Find Restaurant";
        }
    });


function renderRestaurantList(restaurants, container) {

    if (!restaurants || restaurants.length === 0) {
        container.innerHTML = "<p style='color: #888;'>No restaurants found.</p>";
        return;
    }

    container.innerHTML = restaurants.map(r => `
        <div class="profile-card" style="margin-bottom: 1.5rem;">

            <div class="profile-top">
                <div class="profile-avatar">${(r.name || "R").charAt(0).toUpperCase()}</div>
                <div class="profile-main">
                    <h2>${escapeHtml(r.name)}</h2>
                    <p class="customer-code">Code: <span>${escapeHtml(r.restaurantCode)}</span></p>
                </div>
                <div class="status-badge">${r.status || "OPEN"}</div>
            </div>

            <div class="profile-details">

                <div class="profile-item">
                    <span class="profile-label">CUISINE</span>
                    <strong>${escapeHtml(r.cuisine || "—")}</strong>
                </div>

                <div class="profile-item">
                    <span class="profile-label">PHONE</span>
                    <strong>${escapeHtml(r.contactPhone || "—")}</strong>
                </div>

                <div class="profile-item">
                    <span class="profile-label">OPENING HOURS</span>
                    <strong>${escapeHtml(r.openingHours || "—")}</strong>
                </div>

                <div class="profile-item">
                    <span class="profile-label">ADDRESS</span>
                    <strong>${escapeHtml(rstFormatAddress(r.address))}</strong>
                </div>

            </div>

            ${r.menuItems && r.menuItems.length > 0 ? `
            <div class="profile-actions-header">
                <span class="profile-actions-label">Menu (${r.menuItems.length} items)</span>
            </div>
            <div class="profile-details">
                ${r.menuItems.map(item => `
                    <div class="profile-item">
                        <span class="profile-label">${escapeHtml(item.category || "ITEM")}</span>
                        <strong>${escapeHtml(item.name)} — RM ${parseFloat(item.price).toFixed(2)}
                            ${item.available ? "" : " <em style='color:#e55;'>(unavailable)</em>"}
                        </strong>
                    </div>
                `).join("")}
            </div>` : ""}

        </div>
    `).join("");
}


// ------------------------------------------------------------
// ADD RESTAURANT PAGE — Register Restaurant
// ------------------------------------------------------------

document.getElementById("rstRegisterForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        // Clear previous field errors
        document.querySelectorAll("[id^='rst-err-']").forEach(el => el.textContent = "");

        const payload = {
            name:         document.getElementById("rstName").value.trim(),
            description:  document.getElementById("rstDescription").value.trim() || null,
            cuisine:      document.getElementById("rstCuisine").value.trim() || null,
            ownerName:    document.getElementById("rstOwnerName").value.trim(),
            contactPhone: document.getElementById("rstContactPhone").value.trim(),
            contactEmail: document.getElementById("rstContactEmail").value.trim() || null,
            openingHours: document.getElementById("rstOpeningHours").value.trim() || null,
            address: {
                street:   document.getElementById("rstStreet").value.trim(),
                postcode: document.getElementById("rstPostcode").value.trim(),
                city:     document.getElementById("rstCity").value.trim(),
                state:    document.getElementById("rstState").value.trim(),
                country:  document.getElementById("rstCountry").value.trim()
            }
        };

        const btn   = document.getElementById("rstRegisterButton");
        const label = btn.querySelector(".button-label");
        btn.disabled    = true;
        label.textContent = "Registering...";

        try {
            const { data } = await rstApiRequest("", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            currentRestaurant = data;

            showToast(
                `Restaurant registered! Code: ${data.restaurantCode}`,
                "success"
            );

            document.getElementById("rstRegisterForm").reset();
            loadRestaurantForManage(data);

        } catch (error) {

            if (error.fieldErrors) {
                Object.entries(error.fieldErrors).forEach(([field, msg]) => {
                    const el = document.getElementById(`rst-err-${field}`);
                    if (el) el.textContent = msg;
                });
            }

            showToast(error.message, "error");

        } finally {
            btn.disabled = false;
            label.textContent = "Register Restaurant";
        }
    });


// ------------------------------------------------------------
// MANAGE LOOKUP (on the Add Restaurant page)
// ------------------------------------------------------------

document.getElementById("rstManageLookupLink")
    .addEventListener("click", function (event) {
        event.preventDefault();
        document.getElementById("rstManageLookupBar").hidden  = false;
        document.getElementById("rstManageLookupToggle").hidden = true;
        document.getElementById("rstManageCode").focus();
    });

document.getElementById("rstManageLoadButton")
    .addEventListener("click", async function () {

        const code = document.getElementById("rstManageCode").value.trim();

        if (!code) {
            showToast("Enter a restaurant code.", "error");
            return;
        }

        this.disabled = true;
        this.textContent = "Loading...";

        try {
            const { data } = await rstApiRequest(`/${encodeURIComponent(code)}`);
            currentRestaurant = data;
            loadRestaurantForManage(data);
            showToast(`Loaded: ${data.name}`, "success");
        } catch (error) {
            showToast(error.message, "error");
        } finally {
            this.disabled = false;
            this.textContent = "Load Restaurant";
        }
    });


function loadRestaurantForManage(restaurant) {

    currentRestaurant = restaurant;

    const section = document.getElementById("rstMenuSection");
    section.hidden = false;

    document.getElementById("rstMenuSectionTitle").textContent =
        `Menu — ${restaurant.name} (${restaurant.restaurantCode})`;

    renderMenuItemsForManage(restaurant.menuItems || []);
}


// ------------------------------------------------------------
// ADD MENU ITEM
// ------------------------------------------------------------

document.getElementById("rstAddMenuItemForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        if (!currentRestaurant) {
            showToast("No restaurant loaded. Register or look one up first.", "error");
            return;
        }

        const payload = {
            name:        document.getElementById("menuItemName").value.trim(),
            description: document.getElementById("menuItemDescription").value.trim() || null,
            price:       parseFloat(document.getElementById("menuItemPrice").value),
            category:    document.getElementById("menuItemCategory").value.trim() || null
        };

        const btn   = document.getElementById("rstAddMenuItemButton");
        const label = btn.querySelector(".button-label");
        btn.disabled = true;
        label.textContent = "Adding...";

        try {
            await rstApiRequest(
                `/${encodeURIComponent(currentRestaurant.restaurantCode)}/menu`,
                { method: "POST", body: JSON.stringify(payload) }
            );

            showToast("Menu item added!", "success");

            document.getElementById("rstAddMenuItemForm").reset();

            // Reload the restaurant so menu list updates
            const { data } = await rstApiRequest(
                `/${encodeURIComponent(currentRestaurant.restaurantCode)}`
            );
            currentRestaurant = data;
            renderMenuItemsForManage(data.menuItems || []);

        } catch (error) {
            showToast(error.message, "error");
        } finally {
            btn.disabled = false;
            label.textContent = "Add to Menu";
        }
    });


// ------------------------------------------------------------
// RENDER MENU ITEMS LIST (manage view)
// ------------------------------------------------------------

function renderMenuItemsForManage(items) {

    const container = document.getElementById("rstMenuItemsList");

    if (!items || items.length === 0) {
        container.innerHTML = "<p style='color: #888; margin-top: 0.5rem;'>No menu items yet. Add one above.</p>";
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="profile-card" style="margin-bottom: 1rem;">
            <div class="profile-details">
                <div class="profile-item">
                    <span class="profile-label">ITEM</span>
                    <strong>${escapeHtml(item.name)}</strong>
                </div>
                <div class="profile-item">
                    <span class="profile-label">PRICE</span>
                    <strong>RM ${parseFloat(item.price).toFixed(2)}</strong>
                </div>
                <div class="profile-item">
                    <span class="profile-label">CATEGORY</span>
                    <strong>${escapeHtml(item.category || "—")}</strong>
                </div>
                <div class="profile-item">
                    <span class="profile-label">STATUS</span>
                    <strong>${item.available ? "✅ Available" : "❌ Unavailable"}</strong>
                </div>
            </div>
            <div class="profile-actions" style="margin-top: 0.75rem;">
                <div class="action-item">
                    <button class="secondary-button"
                        onclick="rstToggleAvailability('${escapeHtml(item.menuItemCode)}', ${item.available})">
                        ${item.available ? "Mark Unavailable" : "Mark Available"}
                    </button>
                </div>
                <div class="action-item">
                    <button class="danger-button"
                        onclick="rstDeleteMenuItem('${escapeHtml(item.menuItemCode)}')">
                        Remove Item
                    </button>
                </div>
            </div>
        </div>
    `).join("");
}


// ------------------------------------------------------------
// TOGGLE MENU ITEM AVAILABILITY
// ------------------------------------------------------------

async function rstToggleAvailability(menuItemCode, currentlyAvailable) {

    if (!currentRestaurant) return;

    try {
        await rstApiRequest(
            `/${encodeURIComponent(currentRestaurant.restaurantCode)}/menu/${encodeURIComponent(menuItemCode)}`,
            {
                method: "PUT",
                body: JSON.stringify({ available: !currentlyAvailable })
            }
        );

        showToast(
            currentlyAvailable ? "Item marked unavailable." : "Item marked available.",
            "success"
        );

        // Refresh menu list
        const { data } = await rstApiRequest(
            `/${encodeURIComponent(currentRestaurant.restaurantCode)}`
        );
        currentRestaurant = data;
        renderMenuItemsForManage(data.menuItems || []);

    } catch (error) {
        showToast(error.message, "error");
    }
}


// ------------------------------------------------------------
// DELETE MENU ITEM
// ------------------------------------------------------------

async function rstDeleteMenuItem(menuItemCode) {

    if (!currentRestaurant) return;

    if (!confirm("Remove this menu item?")) return;

    try {
        await rstApiRequest(
            `/${encodeURIComponent(currentRestaurant.restaurantCode)}/menu/${encodeURIComponent(menuItemCode)}`,
            { method: "DELETE" }
        );

        showToast("Menu item removed.", "success");

        const { data } = await rstApiRequest(
            `/${encodeURIComponent(currentRestaurant.restaurantCode)}`
        );
        currentRestaurant = data;
        renderMenuItemsForManage(data.menuItems || []);

    } catch (error) {
        showToast(error.message, "error");
    }
}


// ------------------------------------------------------------
// HELPERS
// ------------------------------------------------------------

function rstFormatAddress(address) {
    if (!address) return "—";
    return [address.street, address.postcode, address.city, address.state, address.country]
        .filter(Boolean)
        .join(", ");
}

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

// ============================================================
// END RESTAURANT & CATALOG SERVICE
// ============================================================
