import React from 'react';
import dayjs from 'dayjs';
import { Link } from 'react-router-dom';
import Api from '../apis/api';

function Page() {
  const roomsQuery = Api.Room.Query.useRoomsQuery(
    Api.serializePageableParams({
      limit: 5000,
      sort: {
        id: Api.Pageable.Order.DESC,
      },
    }),
  );
  return (
    <div className="mx-auto w-full max-w-[1200px] py-50">
      <div className="flex flex-col [&>a:not(:last-child)]:border-b [&>a:not(:last-child)]:border-solid [&>a:not(:last-child)]:border-b-[#E5E7EB]">
        {roomsQuery.data?.data.results.map((response) => {
          return (
            <Link
              key={`Room-${response.id}`}
              to={`/room/${response.id}`}
              className="flex items-center gap-24 py-18 no-underline"
            >
              <span className="basis-50 text-14 font-[600] text-[#111827]">
                {response.id}
              </span>
              <span className="flex-1 text-14 font-[600] text-[#111827]">
                {response.title}
              </span>
              <div className="flex flex-col gap-8">
                <span className="text-right text-14 font-[400] text-[#111827]">
                  {response.createdBy.name}
                </span>
                <span className="text-right text-14 font-[400] text-[#6B7280]">
                  {dayjs(response.createdAt).fromNow()}
                </span>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}

export default Page;
