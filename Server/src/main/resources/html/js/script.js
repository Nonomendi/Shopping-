const domainName = "http://localhost:5000";
let userID = getSession();

async function deleteProduct() {
    const urlParams = new URLSearchParams(window.location.search);

    await fetch(domainName + "/remove-products", {
        method: 'POST',
        headers: {
            'Accept': "application/json",
        },
        body: JSON.stringify([urlParams.get("id")])
    });

    window.location.replace("./index.html")
}

async function deleteProducts() {
    const ch_list = Array();
    let checkboxes = document.querySelectorAll('input[type=checkbox]:checked');

    for (let i = 0; i < checkboxes.length; i++) {
        ch_list.push(checkboxes[i].value);
    }

    await fetch(domainName + "/remove-products", {
        method: 'POST',
        headers: {
            'Accept': "application/json",
        },
        body: JSON.stringify(ch_list)
    });

    window.location.replace("./index.html")
}

function editFormView() {
    const urlParams = new URLSearchParams(window.location.search);

    document.getElementById("item_update").innerHTML = '<form action="product_update.html" id="update_item">' +
        '<label for="update_name"></label>' +
        '<input id="update_name" type="text" name="update_name" placeholder="Enter product name" required><br/>' +
        '<label for="update_desc"></label>' +
        '<input id="update_desc" type="text" name="update_desc" placeholder="Enter product description" required><br/>' +
        '<label for="update_price"></label>' +
        '<input id="update_price" type="text" name="update_price" placeholder="Enter product price omitting currency sign" required><br/>' +
        '<input type="hidden" name="id" value="' + urlParams.get("id") + '">' +
        '<input type="submit" value="Update product">' +
        '</form>';
}

async function viewProduct() {
    const urlParams = new URLSearchParams(window.location.search);

    await fetch(domainName + "/product/" + urlParams.get("id"), {
        method: 'GET',
        headers: {
            'Accept': "application/json",
        }
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("item-name").innerText = data.name;
            document.getElementById("item-description").innerText = data.description;
            document.getElementById("item-price").innerText = `R ${data.price}`;
        });
}

async function updateProduct() {
    const urlParams = new URLSearchParams(window.location.search);
    let item_name = urlParams.get("update_name");
    let item_desc = urlParams.get("update_desc");
    let item_price = urlParams.get("update_price");
    let item_id = urlParams.get("id");

    await fetch(domainName + "/product/" + item_id, {
        method: "PUT",
        headers: {
            'Accept': "application/json",
        },
        body: JSON.stringify({id: item_id, name: item_name, description: item_desc, price: +item_price})
    }).then(response => response.json())
        .then(data => console.log(data));
}

async function openStore() {
    await fetch(`${domainName}/products`, {
        method: 'POST',
        headers: {
            'Accept': "application/json",
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.length <= 0) {
                let result = document.getElementById("results");
                result.setAttribute("id", "noItemsText");
                result.innerText = "There are currently no items/products available for purchase";
            } else {
                displayResults(data);
            }
        });
}

function displayResults(data) {
    const resultsContainer = document.getElementById('results');

    let table = document.createElement("table");

    const headings = document.createElement('tr');
    const headingItem = document.createElement('th');
    headingItem.append("Item");
    const headingDesc = document.createElement('th');
    headingDesc.append("Description");
    const headingPrice = document.createElement('th');
    headingPrice.append("Price");
    const headingDelete = document.createElement('th');
    headingDelete.append("Delete");
    const headingWish = document.createElement('th');
    headingWish.append("Add to cart");

    headings.append(headingItem);
    headings.append(headingDesc);
    headings.append(headingPrice);
    headings.append(headingDelete);
    headings.append(headingWish);
    table.append(headings);

    data.forEach(item => {
        const product = JSON.parse(item);

        const productRow = document.createElement('tr');
        const productID = document.createElement('a');
        const productItem = document.createElement('td');
        const productDesc = document.createElement('td');
        const productPrice = document.createElement('td');
        const productDelete = document.createElement('td');
        const productWish = document.createElement('td');

        const deleteCheckbox = document.createElement("input");
        deleteCheckbox.type="checkbox";
        deleteCheckbox.value=`${product.id}`;
        const wishButton = document.createElement("input");
        wishButton.type="submit";
        wishButton.id=`${product.id}`;
        wishButton.value = 'Add to cart';

        wishButton.onclick = addToCart;

        productID.setAttribute('href', `./product.html?id=${product.id}`);
        productID.innerText = `${product.name}`;
        productItem.appendChild(productID);
        productDesc.append(`${product.description}`);
        productPrice.append(`R${product.price}`);
        productDelete.append(deleteCheckbox);
        productWish.append(wishButton);

        productRow.append(productItem);
        productRow.append(productDesc);
        productRow.append(productPrice);
        productRow.append(productDelete);
        productRow.append(productWish);
        table.append(productRow);
    });

    resultsContainer.append(table)
}

async function productListTable(data) {
    let table = document.createElement("table");

    const headings = document.createElement('tr');
    let prodName = document.createElement("th");
    prodName.append("Product Name");
    let prodPrice = document.createElement("th");
    prodPrice.append("Price");

    headings.append(prodName);
    headings.append(prodPrice);
    table.append(headings);

    for (let i = 0; i < Array.from(data.products).length; i++) {
        await fetch(domainName + "/product/" + Array.from(data.products)[i], {
            method: 'GET',
            headers: {
                'Accept': "application/json",
            }
        })
            .then(response => response.json())
            .then(data => {
                let row = document.createElement("tr");

                let productName = document.createElement("td");
                let productPrice = document.createElement("td");
                productName.append(data.name);
                productPrice.append(`R ${data.price}`);

                row.append(productName);
                row.append(productPrice);
                table.append(row);
            });
    }

    let total = document.createElement("tr");
    let totalPrice = document.createElement("td");
    totalPrice.append(`R ${data.total}`)
    total.append(document.createElement("td"));
    total.append(totalPrice);
    table.append(total);
    return table;
}

function addProduct() {
    let productForm = document.getElementById("product_add_form");

    productForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        let productName = document.getElementById("product_name");
        let productDescription = document.getElementById("product_desc");
        let productPrice = document.getElementById("product_price");

        await fetch(domainName + "/product", {
            method: 'POST',
            headers: { 'Accept': "application/json", },
            body: JSON.stringify({name: productName.value, description: productDescription.value, price: productPrice.value})
        }).then(response => response.json())
            .then(data => {
                console.log(data);
                if (data.code === "BAD_REQUEST") {
                    alert(data.message + " invalid data entry");
                } else {
                    window.location.replace("./index.html");
                }
            });
    });
}

async function addToCart(e) {
    if (userID == null) {
        alert("You need to be logged in to perform this action");
    } else {
        await fetch(domainName + "/order", {
            method: 'POST',
            headers: { 'Accept': "application/json", },
            body: JSON.stringify({customerId: userID, products: e.target.id})
        });
    }
}

async function getActiveOrder() {
    await fetch(domainName + "/orders", {
        method: 'POST',
        headers: { 'Accept': "application/json", },
        body: JSON.stringify({customerId: userID})
    })
        .then(response => response.json())
        .then(async data => {
            let content = document.getElementById("basket");
            content.append(await productListTable(data));

            let payment = document.getElementById("payment");
            payment.innerHTML = `<form action="./checkout.html" method="post" id="make_payment">
                                    <input type="hidden" value="${data.total}" name="amount">
                                    <input type="hidden" value="${data.id}" name="order_id">
                                    <input type="submit" value="Pay R ${data.total} now">
                                 </form>`;

            document.getElementById("make_payment").addEventListener("submit", async function(evt){
                evt.preventDefault();

                data.paid = true;
                await fetch(domainName + "/order/" + data.id, {
                    method: 'PUT',
                    headers: {'Accept': "application/json",},
                    body: JSON.stringify(data),
                }).then(response => response.json())
                    .then(data => {
                        if (data.paid) {
                            window.location.replace("./checkout.html?id=" + data.id);
                        } else {
                            content.innerHTML = "Failed to forward you to the checkout page";
                            payment.innerHTML = "<a href='./checkout.html?id='" +  + data.id + ">Click here</a> to got to order confirmation."
                        }
                    });
            });
        });
}

async function confirmPayment() {
    const urlParams = new URLSearchParams(window.location.search);
    await fetch(domainName + "/order/" + urlParams.get("id"), {
        method: 'GET',
        headers: {'Accept': "application/json",},
    }).then(response => response.json())
        .then(async data => {
            document.getElementById('amount').innerText = data.total;
            let product_list = document.getElementById('product_list');
            product_list.append(await productListTable(data));
        });
}
//
// async function getHistoryOrders() {
//
// }

async function loginPage() {
    const urlParams = new URLSearchParams(window.location.search);

    if (userID != null) {
        let manage = document.getElementById("login_form");

        manage.innerHTML = '<div style="color: blue; cursor: pointer" onclick="deleteAccount()">Delete account</div><br/>' +
            '<div style="color: blue; cursor: pointer" onclick="signOut()">Log out</div>'
    } else if (urlParams.size === 2) {
        await fetch(domainName + "/customer", {
            method: 'POST',
            headers: { 'Accept': "application/json", },
            body: JSON.stringify({name: urlParams.get("name"), email: urlParams.get("email")})
        }).then(response => response.json())
            .then(data => {
                if (data.code === "BAD_REQUEST") {
                    alert("The email address you tried to use is being used by another user");
                    window.location.replace("./login.html");
                } else {
                    setSession(data.id);
                    window.location.reload();
                }
            });
    }
}

async function deleteAccount() {
    await fetch(domainName + "/customer/" + userID);
    signOut();
}

function signOut() {
    userID = null;
    localStorage.removeItem("userId");
    window.location.replace("./index.html");
}

function setSession(id) {
    userID = id;
    localStorage.setItem("userId", id);
}

function getSession() {
    return localStorage.getItem("userId");
}