const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await (await browser.newContext({
    viewport: { width: 1440, height: 900 }, locale: 'zh-CN'
  })).newPage();
  const DIR = 'D:/CLion/oa-system/doc/screenshots';

  // Login
  console.log('Login...');
  await page.goto('http://localhost:5173/#/login', { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  await page.fill('input[placeholder*="用户名"]', 'admin');
  await page.fill('input[placeholder*="密码"]', '123456');
  await page.click('button:has-text("登 录")');
  await page.waitForURL('**/dashboard', { timeout: 15000 });
  console.log('OK');

  // Navigate via sidebar: 系统管理 → 角色管理
  // (This approach works; direct URL doesn't)
  console.log('Sidebar nav to role page...');
  await page.goto('http://localhost:5173', { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  const sysBtn = page.locator('.el-sub-menu__title').filter({ hasText: '系统管理' });
  if (await sysBtn.count() > 0) { await sysBtn.first().click(); await page.waitForTimeout(500); }
  const roleItem = page.locator('.el-menu-item').filter({ hasText: '角色管理' });
  if (await roleItem.count() > 0) { await roleItem.first().click(); }
  await page.waitForTimeout(2500);
  console.log('Page:', await page.locator('.page-header span, .el-card__header span').first().textContent());

  const roles = [
    { name: '超级管理员', file: 'admin-menu.png' },
    { name: '部门经理', file: 'manager-menu.png' },
    { name: '普通员工', file: 'employee-menu.png' }
  ];

  for (const r of roles) {
    console.log(`\n=== ${r.name} ===`);

    // Click "分配菜单" in the row with this role name
    const clicked = await page.evaluate((roleName) => {
      // Element Plus tables use .el-table__row class
      const rows = document.querySelectorAll('.el-table__row, tbody tr.el-table__row');
      for (const row of rows) {
        if (row.textContent.includes(roleName)) {
          const btns = row.querySelectorAll('button');
          for (const btn of btns) {
            if (btn.textContent.includes('分配菜单')) {
              btn.click();
              return true;
            }
          }
        }
      }
      // Fallback: try all tr elements
      const allRows = document.querySelectorAll('tr');
      for (const row of allRows) {
        if (row.textContent.includes(roleName) && row.textContent.includes('ROLE_')) {
          const btns = row.querySelectorAll('button');
          for (const btn of btns) {
            if (btn.textContent.includes('分配菜单')) {
              btn.click();
              return true;
            }
          }
        }
      }
      return false;
    }, r.name);
    console.log(`  Clicked: ${clicked}`);

    if (!clicked) continue;

    // Wait for dialog + tree + setTimeout(200) + getRoleById API + setCheckedKeys
    await page.waitForTimeout(4000);

    // Verify dialog state
    const dlgInfo = await page.evaluate(() => {
      const dlgs = document.querySelectorAll('.el-dialog');
      for (const dlg of dlgs) {
        const style = window.getComputedStyle(dlg);
        if (style.display !== 'none' && dlg.offsetParent !== null) {
          const checks = dlg.querySelectorAll('.el-checkbox.is-checked');
          const title = dlg.querySelector('.el-dialog__title');
          return {
            title: title ? title.textContent : 'no-title',
            checked: checks.length
          };
        }
      }
      return { title: 'no-visible-dialog', checked: 0 };
    });
    console.log(`  Dialog: ${JSON.stringify(dlgInfo)}`);

    // Screenshot
    await page.screenshot({ path: path.join(DIR, r.file), fullPage: true });
    console.log(`  Saved: ${r.file} (${fs.statSync(path.join(DIR, r.file)).size} bytes)`);

    // Close dialog
    await page.keyboard.press('Escape');
    await page.waitForTimeout(800);
  }

  await browser.close();
  console.log('\nDone!');
})();
