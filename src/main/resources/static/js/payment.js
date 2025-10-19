const amount = 0;
const availableCredit = 0;
let creditApplied = 0;
let creditUsed = false;

function applyCredit(){
    if(creditUsed){
        alert("Credit already applied.");
        return;
    }

    //calculate how much credit to apply
    creditApplied = Math.min(availableCredit, amount);
    const finalAmount = amount - creditApplied;

    //update display
    document.getElementById("finalAmount").textContent = finalAmount.toFixed(2);
    document.getElementById("creditAppliedAmount").textContent = creditApplied.toFixed(2);
    document.getElementById("creditAppliedMsg").style.display = "block";
    document.getElementById("creditApplied").value = creditApplied;

    // Update available credit display
    const remainingCredit = availableCredit - creditApplied;
    document.getElementById("availableCredit").textContent = remainingCredit.toFixed(2);

    //disable button
    const btn = document.getElementById("applyCreditBtn");
    btn.disabled = true;
    btn.textContent = "Credit Applied";
    btn.style.opacity = '0.6';
    btn.style.cursor = 'not-allowed';

    creditUsed = true;

}

