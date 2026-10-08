import { CapacitorUpdater } from '@capgo/capacitor-updater';
import { Capacitor } from '@capacitor/core';
import { LaunchNavigator, LaunchMode, TransportMode } from '@capgo/capacitor-launch-navigator';

const output = document.getElementById('plugin-output');
const chipPlatform = document.getElementById('chip-platform');
const chipDefaultApp = document.getElementById('chip-default-app');
const chipAppsCount = document.getElementById('chip-apps-count');
const chipVersion = document.getElementById('chip-version');
const appPicker = document.getElementById('app-picker');
const appsList = document.getElementById('apps-list');

let cachedApps = [];

const setOutput = (value) => {
  if (!output) {
    return;
  }
  output.textContent = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
};

const logAction = (label, result) => {
  const stamp = new Date().toISOString();
  setOutput(`[${stamp}] ${label}\n${typeof result === 'string' ? result : JSON.stringify(result, null, 2)}`);
};

const parseCoordinate = (raw, label) => {
  if (raw === '' || raw === null || raw === undefined) {
    return undefined;
  }
  const value = Number(raw);
  if (!Number.isFinite(value)) {
    throw new Error(`${label} must be a valid number`);
  }
  return value;
};

const readDestination = () => {
  const lat = parseCoordinate(document.getElementById('destination-lat')?.value, 'Destination latitude');
  const lng = parseCoordinate(document.getElementById('destination-lng')?.value, 'Destination longitude');
  if (lat === undefined || lng === undefined) {
    throw new Error('Destination latitude and longitude are required');
  }
  return { lat, lng };
};

const readNavigateOptions = () => {
  const destinationName = document.getElementById('destination-name')?.value?.trim();
  const startLat = parseCoordinate(document.getElementById('start-lat')?.value, 'Start latitude');
  const startLng = parseCoordinate(document.getElementById('start-lng')?.value, 'Start longitude');
  const startName = document.getElementById('start-name')?.value?.trim();
  const transportMode = document.getElementById('transport-mode')?.value;
  const launchMode = document.getElementById('launch-mode')?.value;
  const app = appPicker?.value;
  const enableDebug = document.getElementById('enable-debug')?.checked === true;

  const options = {};
  if (destinationName) {
    options.destinationName = destinationName;
  }
  if (startLat !== undefined && startLng !== undefined) {
    options.start = [startLat, startLng];
  }
  if (startName) {
    options.startName = startName;
  }
  if (transportMode && Object.values(TransportMode).includes(transportMode)) {
    options.transportMode = transportMode;
  }
  if (launchMode && Object.values(LaunchMode).includes(launchMode)) {
    options.launchMode = launchMode;
  }
  if (app) {
    options.app = app;
  }
  if (enableDebug) {
    options.enableDebug = true;
  }

  return options;
};

const renderApps = (apps) => {
  cachedApps = apps;
  if (!appsList) {
    return;
  }
  appsList.replaceChildren();
  if (!apps.length) {
    const empty = document.createElement('li');
    empty.className = 'apps-empty';
    empty.textContent = 'No apps reported. Tap Refresh list.';
    appsList.appendChild(empty);
    return;
  }

  for (const entry of apps) {
    const item = document.createElement('li');
    item.className = 'app-item';
    if (!entry.available) {
      item.classList.add('unavailable');
    }

    const main = document.createElement('button');
    main.type = 'button';
    main.className = 'app-select';
    main.dataset.app = entry.app;
    main.innerHTML = `<strong>${entry.name || entry.app}</strong><span class="app-id">${entry.app}</span>`;

    const badge = document.createElement('span');
    badge.className = entry.available ? 'status-pill ok' : 'status-pill off';
    badge.textContent = entry.available ? 'Installed' : 'Not installed';

    main.addEventListener('click', () => {
      if (appPicker) {
        appPicker.value = entry.app;
      }
      logAction(`Selected app: ${entry.app}`, entry);
    });

    item.append(main, badge);
    appsList.appendChild(item);
  }
};

const populateAppPicker = (apps) => {
  if (!appPicker) {
    return;
  }
  const previous = appPicker.value;
  appPicker.replaceChildren();
  const defaultOption = document.createElement('option');
  defaultOption.value = '';
  defaultOption.textContent = 'System default or chooser';
  appPicker.appendChild(defaultOption);

  for (const entry of apps) {
    const option = document.createElement('option');
    option.value = entry.app;
    option.textContent = entry.available
      ? `${entry.name || entry.app} (${entry.app})`
      : `${entry.name || entry.app} (not installed)`;
    option.disabled = !entry.available;
    appPicker.appendChild(option);
  }

  if (previous && apps.some((app) => app.app === previous && app.available)) {
    appPicker.value = previous;
  }
};

const refreshApps = async () => {
  try {
    const result = await LaunchNavigator.getAvailableApps();
    const apps = result.apps || [];
    renderApps(apps);
    populateAppPicker(apps);
    if (chipAppsCount) {
      const installed = apps.filter((app) => app.available).length;
      chipAppsCount.textContent = `${installed} installed / ${apps.length} supported`;
    }
    logAction('getAvailableApps', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
};

const refreshChips = async () => {
  if (chipPlatform) {
    chipPlatform.textContent = Capacitor.getPlatform();
  }
  try {
    const defaultApp = await LaunchNavigator.getDefaultApp();
    if (chipDefaultApp) {
      chipDefaultApp.textContent = `Default: ${defaultApp.app}`;
    }
  } catch (error) {
    if (chipDefaultApp) {
      chipDefaultApp.textContent = 'Default: unknown';
    }
  }
  try {
    const version = await LaunchNavigator.getPluginVersion();
    if (chipVersion) {
      chipVersion.textContent = `v${version.version}`;
    }
  } catch {
    if (chipVersion) {
      chipVersion.textContent = 'Version unknown';
    }
  }
};

document.getElementById('launch-navigate')?.addEventListener('click', async () => {
  try {
    const { lat, lng } = readDestination();
    const options = readNavigateOptions();
    await LaunchNavigator.navigate({
      destination: [lat, lng],
      options,
    });
    logAction('navigate', { destination: [lat, lng], options, status: 'Launched (no native error thrown)' });
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('refresh-apps')?.addEventListener('click', refreshApps);

document.getElementById('get-supported')?.addEventListener('click', async () => {
  try {
    const result = await LaunchNavigator.getSupportedApps();
    logAction('getSupportedApps', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('get-default')?.addEventListener('click', async () => {
  try {
    const result = await LaunchNavigator.getDefaultApp();
    if (chipDefaultApp) {
      chipDefaultApp.textContent = `Default: ${result.app}`;
    }
    logAction('getDefaultApp', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('check-app')?.addEventListener('click', async () => {
  const app = appPicker?.value;
  if (!app) {
    setOutput('Pick an app in Preferred app or tap one in the list first.');
    return;
  }
  try {
    const result = await LaunchNavigator.isAppAvailable({ app });
    logAction(`isAppAvailable (${app})`, result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('get-icons')?.addEventListener('click', async () => {
  try {
    const apps = cachedApps.length ? cachedApps.map((entry) => entry.app).slice(0, 6) : undefined;
    const result = await LaunchNavigator.getAppIcons(apps ? { apps } : undefined);
    logAction('getAppIcons', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('refresh-icons')?.addEventListener('click', async () => {
  try {
    const apps = cachedApps.length ? cachedApps.map((entry) => entry.app).slice(0, 6) : undefined;
    const result = await LaunchNavigator.refreshAppIcons(apps ? { apps } : undefined);
    logAction('refreshAppIcons', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('clear-icon-cache')?.addEventListener('click', async () => {
  try {
    const result = await LaunchNavigator.clearIconCache();
    logAction('clearIconCache', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('get-version')?.addEventListener('click', async () => {
  try {
    const result = await LaunchNavigator.getPluginVersion();
    if (chipVersion) {
      chipVersion.textContent = `v${result.version}`;
    }
    logAction('getPluginVersion', result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

const boot = async () => {
  await refreshChips();
  await refreshApps();
};

boot();

if (Capacitor.isNativePlatform()) {
  CapacitorUpdater.notifyAppReady().catch((error) => {
    console.error('Capgo notifyAppReady failed', error);
  });
}
