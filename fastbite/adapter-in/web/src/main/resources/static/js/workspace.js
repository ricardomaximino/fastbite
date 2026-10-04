(() => {
'use strict';
const token=document.querySelector('meta[name="csrf-token"]').content;
const header=document.querySelector('meta[name="csrf-header"]').content;
const feedback=document.getElementById('workspace-feedback');
function message(text,error=false){feedback.textContent=text;feedback.hidden=false;feedback.className='alert '+(error?'error':'success');feedback.scrollIntoView({block:'center',behavior:'smooth'});}
async function request(url,body,html=false){const response=await fetch(url,{method:'POST',headers:{[header]:token},body});if(!response.ok || response.redirected)throw new Error('The action could not be completed. Check your session and try again.');return html?response.text():response.json();}
function busy(form,value){form.querySelectorAll('button').forEach(b=>b.disabled=value);form.setAttribute('aria-busy',String(value));}
// Replace only the server-rendered team content, preserving the workspace.
document.addEventListener('submit',async event=>{
 const form=event.target;if(form.id!=='add-staff')return;event.preventDefault();busy(form,true);
 try{document.getElementById('staff-panel').innerHTML=await request('/owner/locations/'+encodeURIComponent(form.dataset.tenant)+'/users',new URLSearchParams(new FormData(form)),true);}catch(e){message(e.message,true);}finally{busy(form,false);}
});
document.addEventListener('click',async event=>{
 const button=event.target.closest('[data-delete-staff]');if(!button)return;
 if(!confirm('Remove this staff account from '+button.dataset.tenant+'? They will lose access to this location.'))return;
 button.disabled=true;try{document.getElementById('staff-panel').innerHTML=await request('/owner/locations/'+encodeURIComponent(button.dataset.tenant)+'/users/delete',new URLSearchParams({userId:button.dataset.user}),true);}catch(e){message(e.message,true);button.disabled=false;}
});
const dialog=document.getElementById('maintenance-dialog');const confirmForm=document.getElementById('maintenance-confirm');const confirmName=document.getElementById('confirmation-name');
let pending=null;let reviewedFile=null;
function review(tenant,action,file){pending={tenant,action,file};confirmForm.reset();confirmName.setCustomValidity('');document.getElementById('maintenance-error').textContent='';document.getElementById('maintenance-title').textContent=action==='clean'?'Reset this restaurant?':action==='restore-demo'?'Load the demo template?':'Restore your backup?';document.getElementById('maintenance-description').textContent=action==='clean'?'This permanently removes this location’s menu, orders, staff and uploaded files.':action==='restore-demo'?'This replaces this location’s menu, settings and files with the sample restaurant and clears existing orders and staff. No sample logins or historical orders are imported.':'This replaces this location’s menu, orders, staff, settings and uploaded files with the backup you checked.';document.getElementById('maintenance-location').textContent=tenant;document.getElementById('maintenance-export').href='/'+encodeURIComponent(tenant)+'/api/backoffice/maintenance/backup';dialog.showModal();confirmName.focus();}
document.querySelectorAll('[data-maintenance]').forEach(button=>button.addEventListener('click',()=>review(button.dataset.tenant,button.dataset.maintenance,null)));
document.getElementById('maintenance-cancel').addEventListener('click',()=>dialog.close());
confirmName.addEventListener('input',()=>confirmName.setCustomValidity(''));
confirmForm.addEventListener('submit',async event=>{event.preventDefault();if(confirmName.value!==pending.tenant){confirmName.setCustomValidity('Enter '+pending.tenant+' exactly to confirm.');confirmName.reportValidity();return;}busy(confirmForm,true);try{let body;if(pending.file){body=new FormData();body.append('file',pending.file);}await request('/'+encodeURIComponent(pending.tenant)+'/api/backoffice/maintenance/'+pending.action,body);dialog.close();window.location.assign('/owner/console?location='+encodeURIComponent(pending.tenant));}catch(e){document.getElementById('maintenance-error').textContent=e.message+' Your setup has not been marked complete.';}finally{busy(confirmForm,false);}});
// Restore the exact File object reviewed, not a subsequently changed selection.
const backupForm=document.getElementById('backup-preview');
if(backupForm){const fileInput=document.getElementById('backup-file');const result=document.getElementById('backup-result');fileInput.addEventListener('change',()=>{reviewedFile=null;result.hidden=true;});backupForm.addEventListener('submit',async event=>{event.preventDefault();result.hidden=true;reviewedFile=null;const file=fileInput.files[0];if(!file)return;busy(backupForm,true);try{const body=new FormData();body.append('file',file);const summary=await request('/owner/workspace/'+encodeURIComponent(backupForm.dataset.tenant)+'/backup-preview',body);if(fileInput.files[0]!==file)return;reviewedFile=file;document.getElementById('backup-summary').textContent='Archive checked: '+summary.products+' menu items, '+summary.orders+' orders and '+summary.accounts+' account records.';result.hidden=false;}catch(e){message('We could not validate this backup. Use an exported FastBite ZIP and check that you are still signed in.',true);}finally{busy(backupForm,false);}});document.getElementById('restore-reviewed').addEventListener('click',()=>{if(reviewedFile)review(backupForm.dataset.tenant,'restore',reviewedFile);});}
function openSection(){if(location.hash==='#add-location'){const item=document.getElementById('add-location');if(item)item.open=true;}}
window.addEventListener('hashchange',openSection);openSection();
})();
