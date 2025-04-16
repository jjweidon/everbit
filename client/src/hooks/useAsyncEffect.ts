import { useEffect, useRef } from 'react';

export function useAsyncEffect(
  effect: () => Promise<void>,
  deps: React.DependencyList = []
) {
  const isMounted = useRef(true);

  useEffect(() => {
    isMounted.current = true;

    const execute = async () => {
      if (!isMounted.current) return;
      await effect();
    };

    execute();

    return () => {
      isMounted.current = false;
    };
  }, deps);
} 