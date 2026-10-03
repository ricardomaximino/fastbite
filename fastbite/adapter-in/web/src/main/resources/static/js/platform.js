(() => {
 const send = document.getElementById('demo-send'); if (!send) return;
 const result = document.getElementById('demo-result');
 const samples = {coffee:{service:'TABLE 6',title:'One lovely coffee break.',items:'1 flat white + 1 butter croissant',total:'€6.00'},lunch:{service:'TAKEAWAY · SAMPLE PAYMENT CONFIRMED',title:'Lunch, ready to go.',items:'1 toasted sandwich + 1 fresh lemonade',total:'€10.50'}};
 let chosen='coffee';
 document.querySelectorAll('[data-demo]').forEach(button => button.addEventListener('click', () => {
  chosen=button.dataset.demo;
  document.querySelectorAll('[data-demo]').forEach(option=>option.setAttribute('aria-pressed',String(option===button)));
  ['service','title','items','total'].forEach(key=>document.getElementById('demo-'+key).textContent=samples[chosen][key]);
  result.textContent='Your kitchen ticket will appear here.';result.classList.remove('sent');
 }));
 send.addEventListener('click',()=>{result.textContent='✓ Sample order #024 · Preparing · '+samples[chosen].items+'. Your team can now follow it through to collection.';result.classList.add('sent');});
})();
