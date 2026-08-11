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
