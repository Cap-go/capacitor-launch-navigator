import { registerPlugin } from '@capacitor/core';

import type { LaunchNavigatorPlugin } from './definitions';

const LaunchNavigator = registerPlugin<LaunchNavigatorPlugin>('LaunchNavigator', {
  web: () => import('./web').then((m) => new m.LaunchNavigatorWeb()),
});

export * from './definitions';
export { LaunchNavigator };
