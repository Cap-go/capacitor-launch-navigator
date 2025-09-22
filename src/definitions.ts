export interface LaunchNavigatorPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
