import { WebPlugin } from '@capacitor/core';

import type { LaunchNavigatorPlugin } from './definitions';

export class LaunchNavigatorWeb extends WebPlugin implements LaunchNavigatorPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
