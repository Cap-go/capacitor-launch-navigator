import { registerPlugin } from '@capacitor/core';

import type { LaunchNavigatorPlugin, LaunchNavigatorPluginIcons } from './definitions';

const LaunchNavigator = registerPlugin<LaunchNavigatorPlugin & LaunchNavigatorPluginIcons>('LaunchNavigator', {
  web: () => import('./web').then((m) => new m.LaunchNavigatorWeb()),
});

export * from './definitions';
export { LaunchNavigator };
