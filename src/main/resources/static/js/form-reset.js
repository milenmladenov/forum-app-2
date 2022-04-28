function form-reset() {
   let inputs = document.querySelectorAll("input");
   inputs.forEach((input) => (input.value = ""));
   }