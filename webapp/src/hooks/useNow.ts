import React from 'react';
import dayjs, { Dayjs } from 'dayjs';

function useNow(timeout: number = 1000) {
  const [now, setNow] = React.useState<Dayjs>(dayjs());

  React.useEffect(() => {
    const interval = setInterval(() => {
      setNow(dayjs());
    }, timeout);
    return () => {
      clearInterval(interval);
    };
  }, []);

  return now;
}

export default useNow;
