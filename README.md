# tickets-bot-akka

Akka based Bot for tracking railway tickets

Actors hierarchy:


1) Talks - route (or create) particular bot talks per client chat.
2) Talk - responsible for messages deduplication and general supervising stategy for bot behavior.
3) DefineRouteTalk - dialogue on the setuping search criteria


                  Talks
                /     \
             Talk     TicketsSearchers
           /
       DefineRouteTalk
