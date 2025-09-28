function toggleFields(){
    const type = document.getElementById("paymentType").value;
    const cardFields = document.getElementById("cardFields");
    cardFields.style.display = (type === "CREDIT_CARD") ? "block" : "none";
}

toggleFields();